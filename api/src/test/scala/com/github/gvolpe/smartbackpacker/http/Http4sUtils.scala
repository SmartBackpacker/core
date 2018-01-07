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

package com.github.gvolpe.smartbackpacker.http

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import org.http4s.server.AuthMiddleware
import org.http4s.{EntityBody, Request}

object Http4sUtils {

  private val authUser: Kleisli[OptionT[IO, ?], Request[IO], String] =
    Kleisli(_ => OptionT.liftF(IO("access_token")))

  val middleware: AuthMiddleware[IO, String] = AuthMiddleware(authUser)

  implicit class ByteVector2String(body: EntityBody[IO]) {
    def asString: String = {
      val array = body.compile.toVector.unsafeRunSync().toArray
      new String(array.map(_.toChar))
    }
  }

}
