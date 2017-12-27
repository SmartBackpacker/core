package com.github.gvolpe.smartbackpacker.common

trait Log[F[_]] {
  def info(value: String): F[Unit]
  def error(error: Throwable): F[Unit]
}