package com.github.gvolpe.smartbackpacker.http

import cats.effect.Effect
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl

class BaseHttpEndpoint[F[_] : Effect] extends Http4sClientDsl[F] {
  val effectDsl: Http4sDsl[F] = Http4sDsl[F]
}