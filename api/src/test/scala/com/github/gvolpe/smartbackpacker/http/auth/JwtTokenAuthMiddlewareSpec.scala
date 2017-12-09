package com.github.gvolpe.smartbackpacker.http.auth

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
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
