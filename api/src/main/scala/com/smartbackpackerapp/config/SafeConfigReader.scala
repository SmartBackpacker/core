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

package com.smartbackpackerapp.config

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

  def list(key: String): List[String] = {
    import scala.collection.JavaConverters._
    safeRead[java.util.List[String]](config.getStringList)(key).toList.flatMap(_.asScala)
  }

  def objectKeyList(key: String): List[String] = {
    import scala.collection.JavaConverters._
    Try {
      config.getAnyRef(key)
        .asInstanceOf[java.util.Map[String, java.util.List[String]]]
        .asScala.keys.toList
    } match {
      case Failure(error) =>
        log.warn(s"Key $key not found: ${error.getMessage}.")
        List.empty[String]
      case Success(values) =>
        values
    }
  }

  def objectMap(key: String): Map[String, String] = {
    import scala.collection.JavaConverters._
    Try {
      config.getAnyRef(key)
        .asInstanceOf[java.util.Map[String, String]]
        .asScala
        .toMap
    } match {
      case Failure(error) =>
        log.warn(s"Key $key not found: ${error.getMessage}.")
        Map.empty[String, String]
      case Success(map) => map
    }
  }

  def objectMapOfList(key: String): Map[String, List[String]] = {
    import scala.collection.JavaConverters._
    Try {
      config.getAnyRef(key)
        .asInstanceOf[java.util.Map[String, java.util.List[String]]]
        .asScala
        .map(kv => (kv._1, kv._2.asScala.toList))
        .toMap
    } match {
      case Failure(error) =>
        log.warn(s"Key $key not found: ${error.getMessage}.")
        Map.empty[String, List[String]]
      case Success(map) => map
    }
  }

}