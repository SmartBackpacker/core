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

import cats.effect.Async
import com.smartbackpackerapp.airlines.parser.{AirlineFile, AirlinesFileParser, AllowanceFile}
import com.smartbackpackerapp.airlines.sql.AirlinesInsertData
import doobie.util.transactor.Transactor

class AirlinesModule[F[_] : Async] {

  val devDbUrl: String  = sys.env.getOrElse("JDBC_DATABASE_URL", "")
  val dbUrl: String     = sys.env.getOrElse("SB_DB_URL", "jdbc:postgresql:sb")

  private val dbDriver  = sys.env.getOrElse("SB_DB_DRIVER", "org.postgresql.Driver")
  private val dbUser    = sys.env.getOrElse("SB_DB_USER", "postgres")
  private val dbPass    = sys.env.getOrElse("SB_DB_PASSWORD", "")

  private val xa = {
    if (devDbUrl.nonEmpty) Transactor.fromDriverManager[F](dbDriver, devDbUrl)
    else Transactor.fromDriverManager[F](dbDriver, dbUrl, dbUser, dbPass)
  }

  def airlinesInsertData(airlineFile: AirlineFile,
                         allowanceFile: AllowanceFile): AirlinesInsertData[F] = {
    val parser = AirlinesFileParser[F](airlineFile, allowanceFile)
    new AirlinesInsertData[F](xa, parser)
  }

}
