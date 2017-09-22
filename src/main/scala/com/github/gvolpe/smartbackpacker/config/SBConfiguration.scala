package com.github.gvolpe.smartbackpacker.config

import com.typesafe.config.ConfigFactory

object SBConfiguration {

  private lazy val configuration  = ConfigFactory.load("smart-backpacker")
  private lazy val safeConfig     = new SafeConfigReader(configuration)

  def page(countryName: String): Option[String] = {
    safeConfig.string("visa-requirements.page." + countryName)
  }

}
