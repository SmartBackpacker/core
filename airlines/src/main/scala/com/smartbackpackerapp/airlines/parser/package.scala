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

package com.smartbackpackerapp.airlines

import scala.util.Try

package object parser {

  case class AirlineFile(value: String) extends AnyVal
  case class AllowanceFile(value: String) extends AnyVal

  case class AirlineDTO(name: String,
                        extra: Option[String],
                        website: Option[String])

  case class AirlineAllowanceDTO(name: String,
                                 baggageType: String,
                                 kgs: Option[Int],
                                 height: Option[Int],
                                 width: Option[Int],
                                 depth: Option[Int])

  implicit class StringOptionOps(value: String) {
    def toOption: Option[String] = {
      if (value.trim.isEmpty) None
      else Some(value.trim)
    }

    def toOptionInt: Option[Int] = {
      if (value.trim.isEmpty) None
      else Try(value.trim.toInt).toOption
    }
  }

  implicit class AirlineDtoOps(list: List[String]) {
    def toAirlineDTO: Option[AirlineDTO] = list match {
      case (n :: e :: w :: Nil) =>
        Some(AirlineDTO(n.trim, e.toOption, w.toOption))
      case _ => None
    }
  }

  implicit class AirlineAllowanceDtoOps(list: List[String]) {
    def toAirlineAllowanceDTO: Option[AirlineAllowanceDTO] = list match {
      case (n :: bt :: kg :: h :: w :: d :: Nil) =>
        Some(AirlineAllowanceDTO(n.trim, bt.trim, kg.toOptionInt, h.toOptionInt, w.toOptionInt, d.toOptionInt))
      case _ => None
    }
  }

}
