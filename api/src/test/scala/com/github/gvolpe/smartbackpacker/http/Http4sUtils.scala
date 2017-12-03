package com.github.gvolpe.smartbackpacker.http

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import org.http4s.server.AuthMiddleware
import org.http4s.{EntityBody, Request}

object Http4sUtils {

  private val authUser: Kleisli[OptionT[IO, ?], Request[IO], String] =
    Kleisli(_ => OptionT.liftF(IO("access_token")))

  val middleware: AuthMiddleware[IO, String] = AuthMiddleware(authUser)

  implicit class ByteVector2String(body: EntityBody[IO]) {
    def asString: String = {
      val array = body.runLog.unsafeRunSync().toArray
      new String(array.map(_.toChar))
    }
  }

}
