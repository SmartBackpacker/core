package com.github.gvolpe.smartbackpacker.common.instances

import cats.effect.Sync
import com.github.gvolpe.smartbackpacker.common.Log

object log {

  implicit def syncLogInstance[F[_]](implicit F: Sync[F]): Log[F] =
    new Log[F] {
      override def error(error: Throwable): F[Unit] = F.delay(println(error.getMessage))
      override def info(value: String): F[Unit] = F.delay(println(value))
    }

}
