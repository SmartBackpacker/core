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
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

trait RepositorySpec extends FunSuiteLike with IOChecker with BeforeAndAfterAll {

  def testDbName: String

  override val transactor: Transactor[IO] = TestDBManager.xa(testDbName).unsafeRunSync()

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDBManager.createTables(testDbName).unsafeRunSync()
  }

}
