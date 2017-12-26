package com.github.gvolpe.smartbackpacker.config

import com.typesafe.config.ConfigFactory
import org.scalatest.FunSuite

class SafeConfigReaderSpec extends FunSuite {

  val safeConfig = new SafeConfigReader(ConfigFactory.load("wrong"))

  test("it fails trying to parse a list") {
    assert(safeConfig.objectKeyList("asd").isEmpty)
  }

  test("it fails trying to parse a map") {
    assert(safeConfig.objectMap("asd").isEmpty)
  }

  test("it fails trying to parse a map of list") {
    assert(safeConfig.objectMapOfList("asd").isEmpty)
  }

}
