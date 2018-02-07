/*
 * Copyright 2017 Smart Backpacker App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartbackpackerapp.http

import cats.{Applicative, Monad}
import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.http4s.server.AuthMiddleware
import org.http4s.{EntityBody, Request}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Http4sUtils {

  private def authUser[F[_]](implicit F: Applicative[F]): Kleisli[OptionT[F, ?], Request[F], String] =
    Kleisli(_ => OptionT.liftF(F.pure("access_token")))

  def middleware[F[_]: Monad]: AuthMiddleware[F, String] = AuthMiddleware.apply[F, String](authUser)

  val taskMiddleware: AuthMiddleware[Task, String] = middleware[Task]
  val ioMiddleware: AuthMiddleware[IO, String] = middleware[IO]

  implicit class ByteVector2String(body: EntityBody[IO]) {
    def asString: String = {
      val array = body.compile.toVector.unsafeRunSync().toArray
      new String(array.map(_.toChar))
    }
  }

  implicit class ByteVector2StringTask(body: EntityBody[Task]) {
    def asString: String = {
      val array = Await.result(body.compile.toVector.runAsync, Duration.Inf).toArray
      new String(array.map(_.toChar))
    }
  }

}
