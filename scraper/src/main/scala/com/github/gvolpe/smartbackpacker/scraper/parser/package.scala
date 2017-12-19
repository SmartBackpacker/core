package com.github.gvolpe.smartbackpacker.scraper

package object parser {

  implicit class RicherString(value: String) {
    def noWhiteSpaces: String = value.dropWhile(_.toInt == 160)
  }

  implicit class NonEmptyListOps[A](list: List[A]) {
    def tailOrEmpty: List[A] = list match {
      case (_ :: t) => t
      case Nil      => List.empty[A]
    }
  }

}
