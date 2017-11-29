package com.github.gvolpe.smartbackpacker.http.auth

import cats.Applicative
import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import cats.syntax.functor._
import com.github.gvolpe.smartbackpacker.http.auth.config.AuthConfig
import org.http4s.Credentials.Token
import org.http4s.dsl.Http4sDsl
import org.http4s.{AuthScheme, AuthedService, Request}
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import tsec.jws.mac._
import tsec.mac.imports._
import tsec.mac.imports.JCAMacPure._

class JwtTokenAuthMiddleware[F[_] : Sync](config: AuthConfig) extends Http4sDsl[F] {

  private val onFailure: AuthedService[String, F] =
    Kleisli(req => OptionT.liftF(Forbidden(req.authInfo)))

  def middleware: AuthMiddleware[F, String] =
    AuthMiddleware(JwtTokenAuthMiddleware.authUser(config.jwtKey), onFailure)

}

object JwtTokenAuthMiddleware {

  def bearerTokenFromRequest[F[_]: Applicative](request: Request[F]): OptionT[F, String] =
    OptionT.fromOption[F] {
      request.headers.get(Authorization).collect {
        case Authorization(Token(AuthScheme.Bearer, token)) => token
      }
    }

  def verifyToken[F[_]: Sync](request: Request[F],
                              jwtKey: MacSigningKey[HMACSHA256]): OptionT[F, String] =
    for {
      token       <- bearerTokenFromRequest[F](request)
      verified    <- OptionT.liftF(JWTMacM.verifyAndParse[F, HMACSHA256](token, jwtKey))
      accessToken <- OptionT.fromOption[F](verified.body.subject)
    } yield accessToken

  def authUser[F[_]: Sync](jwtKey: MacSigningKey[HMACSHA256]): Kleisli[F, Request[F], Either[String, String]] =
    Kleisli { request =>
      verifyToken(request, jwtKey).value.map { option =>
        Either.cond[String, String](option.isDefined, option.get, "Unable to authorize token")
      }
    }

}