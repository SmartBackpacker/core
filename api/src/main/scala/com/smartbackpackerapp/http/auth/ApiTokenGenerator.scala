/*
 * Copyright 2017 Smart Backpacker App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartbackpackerapp.http.auth

import cats.effect.{IO, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.smartbackpackerapp.common.IOApp
import tsec.jws.mac.JWSMacCV.genSigner // Needed for JWTMac.buildToString
import tsec.jws.mac.JWTMac
import tsec.jwt.JWTClaims
import tsec.mac.imports.{HMACSHA256, MacSigningKey}

object ApiTokenGenerator extends TokenGeneration[IO] with IOApp {

  override def start(args: List[String]): IO[Unit] =
    for {
      _     <- putStrLn("Generating API Token")
      token <- tokenGenerator
      _     <- putStrLn(token)
    } yield ()

}

class TokenGeneration[F[_]](implicit F: Sync[F]) {

  private val ApiToken  = sys.env.get("SB_API_TOKEN")
  private val ApiKey    = sys.env.get("SB_API_KEY")

  private def generateJwtKey(token: String): F[MacSigningKey[HMACSHA256]] = {
    F.catchNonFatal(HMACSHA256.buildKeyUnsafe(token.getBytes))
  }

  private def generateToken(claims: JWTClaims, jwtKey: MacSigningKey[HMACSHA256]): F[String] =
    JWTMac.buildToString(claims, jwtKey)

  private val ifEmpty: F[String] = F.raiseError(new Exception("Api Token not found"))

  val tokenGenerator: F[String] = ApiToken.fold(ifEmpty) { apiToken =>
    for {
      jwtKey  <- generateJwtKey(apiToken)
      claims  = JWTClaims(subject = ApiKey, expiration = None)
      token   <- generateToken(claims, jwtKey)
    } yield token
  }

}
