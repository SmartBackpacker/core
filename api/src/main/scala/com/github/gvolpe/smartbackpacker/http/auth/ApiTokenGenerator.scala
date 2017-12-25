package com.github.gvolpe.smartbackpacker.http.auth

import cats.effect.{IO, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.common.IOApp
import tsec.jws.mac.JWSMacCV.genSigner // Needed for JWTMac.buildToString
import tsec.jws.mac.JWTMac
import tsec.jwt.JWTClaims
import tsec.mac.imports.{HMACSHA256, MacSigningKey}

object ApiTokenGenerator extends TokenGeneration[IO] with IOApp {

  override def start(args: List[String]): IO[Unit] =
    for {
      _     <- IO { println("Generating API Token") }
      token <- tokenGenerator
      _     <- IO { println(token) }
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
      claims  = JWTClaims.build(subject = ApiKey, expiration = None)
      token   <- generateToken(claims, jwtKey)
    } yield token
  }

}
