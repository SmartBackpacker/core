package com.github.gvolpe.smartbackpacker.common

import cats.effect.IO

object IOAssertion {
  def apply[A](ioa: IO[A]): A = ioa.unsafeRunSync()
  def when[A](predicate: Boolean)(ioa: IO[A]): Unit = {
    if (predicate) apply(ioa)
    else IO(())
  }
}