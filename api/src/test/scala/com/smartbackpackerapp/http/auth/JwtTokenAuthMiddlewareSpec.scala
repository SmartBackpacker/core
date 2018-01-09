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

package com.smartbackpackerapp.http.auth

import cats.effect.IO
import com.smartbackpackerapp.common.IOAssertion
import org.http4s.server.AuthMiddleware
import org.scalatest.{FunSuite, Matchers}

class JwtTokenAuthMiddlewareSpec extends FunSuite with Matchers {

  val token = "insert_here_your_long_long_access_token"

  test("it fails to create an auth middleware") {
    IOAssertion {
      new Middleware[IO](None).middleware.attempt.map { result =>
        assert(result.isLeft)
      }
    }
  }

  test("it create an auth middleware") {
    IOAssertion {
      new Middleware[IO](Some(token)).middleware.map { result =>
        result shouldBe an [AuthMiddleware[IO, String]]
      }
    }
  }

}
