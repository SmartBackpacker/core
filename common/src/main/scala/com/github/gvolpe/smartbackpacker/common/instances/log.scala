package com.github.gvolpe.smartbackpacker.common.instances

import cats.effect.Sync
import com.github.gvolpe.smartbackpacker.common.Log
import org.slf4j.LoggerFactory

object log {

  private val logger = LoggerFactory.getLogger(this.getClass)

  implicit def syncLogInstance[F[_]](implicit F: Sync[F]): Log[F] =
    new Log[F] {
      override def error(error: Throwable): F[Unit] = F.delay(logger.error(error.getMessage, error))
      override def info(value: String): F[Unit] = F.delay(logger.info(value))
    }

}
