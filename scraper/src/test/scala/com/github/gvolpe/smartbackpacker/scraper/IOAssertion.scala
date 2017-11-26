package com.github.gvolpe.smartbackpacker.scraper

import cats.effect.IO

object IOAssertion {
  def apply[A](ioa: IO[A]): A = ioa.unsafeRunSync()
}
