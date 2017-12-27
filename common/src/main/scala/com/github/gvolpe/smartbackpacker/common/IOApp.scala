package com.github.gvolpe.smartbackpacker.common

import cats.effect.IO
import org.joda.time.Instant

trait IOApp {
  def start(args: List[String]): IO[Unit]
  def main(args: Array[String]): Unit = start(args.toList).unsafeRunSync()

  def putStrLn(value: String): IO[Unit] = IO(println(value))
  def getLine: IO[String] = IO(scala.io.StdIn.readLine())
  def getTime: IO[Instant] = IO(Instant.now())
}