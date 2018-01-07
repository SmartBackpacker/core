package com.github.gvolpe.smartbackpacker.common

import cats.effect.IO
import fs2.Stream

object StreamAssertion {
  def apply[A](stream: Stream[IO, A]): Unit = stream.compile.drain.unsafeRunSync()
}
