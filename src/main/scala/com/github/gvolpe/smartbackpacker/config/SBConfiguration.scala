package com.github.gvolpe.smartbackpacker.config

import com.typesafe.config.ConfigFactory

object SBConfiguration {

  private lazy val configuration  = ConfigFactory.load("smart-backpacker")
  private lazy val safeConfig     = new SafeConfigReader(configuration)

  def pageId(countryName: String): Option[Long] = {
    safeConfig.long("visa-requirements.page-id." + countryName)
  }

}
