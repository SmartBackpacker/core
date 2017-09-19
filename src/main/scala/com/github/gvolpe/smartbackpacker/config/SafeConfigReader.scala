package com.github.gvolpe.smartbackpacker.config

import com.typesafe.config.Config
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

class SafeConfigReader(config: Config) {

  private val log = LoggerFactory.getLogger(getClass)

  private def safeRead[A](f: String => A)(key: String): Option[A] =
    Try(f(key)) match {
      case Failure(error) =>
        log.warn(s"Key $key not found: ${error.getMessage}.")
        None
      case Success(value) =>
        Some(value)
    }

  def string(key: String): Option[String] = safeRead[String](config.getString)(key)
  def int(key: String): Option[Int] = safeRead[Int](config.getInt)(key)
  def long(key: String): Option[Long] = safeRead[Long](config.getLong)(key)
  def boolean(key: String): Option[Boolean] = safeRead[Boolean](config.getBoolean)(key)

}