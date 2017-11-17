package com.github.gvolpe.smartbackpacker.airlines

import cats.effect.IO

object AirlinesJob extends App {

//  val program: IO[Unit] = for {
//    _ <- AirlinesCreateTables[IO].run
//    _ <- AirlinesInsertData[IO].run
//  } yield ()

  AirlinesInsertData[IO].run.unsafeRunSync()

}
