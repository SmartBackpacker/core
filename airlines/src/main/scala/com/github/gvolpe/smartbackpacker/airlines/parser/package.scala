package com.github.gvolpe.smartbackpacker.airlines

import scala.util.Try

package object parser {

  class AirlineFile(val value: String) extends AnyVal
  class AllowanceFile(val value: String) extends AnyVal

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
