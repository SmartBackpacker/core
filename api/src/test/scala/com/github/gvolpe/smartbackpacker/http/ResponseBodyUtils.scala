package com.github.gvolpe.smartbackpacker.http

import cats.effect.IO
import org.http4s.EntityBody

object ResponseBodyUtils {

  implicit class ByteVector2String(body: EntityBody[IO]) {
    def asString: String = {
      val array = body.runLog.unsafeRunSync().toArray
      new String(array.map(_.toChar))
    }
  }

}
