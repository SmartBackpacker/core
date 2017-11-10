package com.github.gvolpe.smartbackpacker.persistence

import cats.effect.IO

object DbApp extends App {

  DbConnection[IO].run.unsafeRunSync()

}
