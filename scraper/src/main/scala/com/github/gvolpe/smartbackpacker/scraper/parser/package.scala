package com.github.gvolpe.smartbackpacker.scraper

package object parser {

  implicit class RicherString(value: String) {
    def noWhiteSpaces: String = value.dropWhile(_.toInt == 160)
  }

}
