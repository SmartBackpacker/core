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

import cats.data.{EitherT, Kleisli, OptionT}
import cats.effect.Sync
import cats.syntax.applicativeError._
import cats.syntax.functor._
import com.smartbackpackerapp.http.auth.JwtTokenAuthMiddleware.AuthConfig
import org.http4s.Credentials.Token
import org.http4s.dsl.Http4sDsl
import org.http4s.{AuthScheme, AuthedService, Request}
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import tsec.jws.mac.JWTMac
import tsec.mac.imports._

object JwtTokenAuthMiddleware {
  def apply[F[_] : Sync](apiToken: Option[String]): F[AuthMiddleware[F, String]] =
    new Middleware[F](apiToken).middleware

  case class AuthConfig(jwtKey: MacSigningKey[HMACSHA256])
}

class Middleware[F[_]](apiToken: Option[String])(implicit F: Sync[F]) {

  private val ifEmpty = F.raiseError[AuthMiddleware[F, String]](new Exception("Api Token not found"))

  private def generateJwtKey(token: String): F[MacSigningKey[HMACSHA256]] = {
    F.catchNonFatal(HMACSHA256.buildKeyUnsafe(token.getBytes))
  }

  val middleware: F[AuthMiddleware[F, String]] = apiToken.fold(ifEmpty) { token =>
    generateJwtKey(token).map { jwtKey =>
      val config = AuthConfig(jwtKey)
      new JwtTokenAuthMiddleware[F](config).middleware
    }
  }
}

class JwtTokenAuthMiddleware[F[_] : Sync](config: AuthConfig) extends Http4sDsl[F] {

  private val onFailure: AuthedService[String, F] =
    Kleisli(req => OptionT.liftF(Forbidden(req.authInfo)))

  private def bearerTokenFromRequest(request: Request[F]): OptionT[F, String] =
    OptionT.fromOption[F] {
      request.headers.get(Authorization).collect {
        case Authorization(Token(AuthScheme.Bearer, token)) => token
      }
    }

  private def verifyToken(request: Request[F],
                          jwtKey: MacSigningKey[HMACSHA256]): OptionT[F, String] =
    for {
      token       <- bearerTokenFromRequest(request)
      verified    <- OptionT.liftF(JWTMac.verifyAndParse[F, HMACSHA256](token, jwtKey))
      accessToken <- OptionT.fromOption[F](verified.body.subject)
    } yield accessToken

  private def authUser(jwtKey: MacSigningKey[HMACSHA256]): Kleisli[F, Request[F], Either[String, String]] =
    Kleisli { request =>
      verifyToken(request, jwtKey).value.map { option =>
        Either.cond[String, String](option.isDefined, option.get, "Unable to authorize token")
      }.recoverWith {
        case MacVerificationError(msg) => EitherT.leftT(msg).value
      }
    }

  def middleware: AuthMiddleware[F, String] =
    AuthMiddleware(authUser(config.jwtKey), onFailure)

}