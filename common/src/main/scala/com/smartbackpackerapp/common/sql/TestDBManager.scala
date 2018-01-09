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

package com.smartbackpackerapp.common.sql

import cats.effect.IO
import doobie.h2.H2Transactor
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway

object TestDBManager {

  private def testDbUrl(dbName: String): String =
    s"jdbc:h2:mem:test_sb_$dbName;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"

  private val testDbUser = "sa"
  private val testDbPass = ""

  def xa(dbName: String): IO[Transactor[IO]] =
    H2Transactor.newH2Transactor[IO](testDbUrl(dbName), testDbUser, testDbPass)

  def createTables(dbName: String): IO[Unit] =
    IO {
      val flyway = new Flyway
      flyway.setDataSource(testDbUrl(dbName), testDbUser, testDbPass)
      flyway.migrate()
    }

}
