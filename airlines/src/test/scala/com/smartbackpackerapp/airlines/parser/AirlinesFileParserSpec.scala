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

package com.smartbackpackerapp.airlines.parser

import cats.effect.IO
import com.smartbackpackerapp.common.StreamAssertion
import org.scalatest.{FlatSpecLike, Matchers}

class AirlinesFileParserSpec extends FlatSpecLike with Matchers {

  private val parser = AirlinesFileParser[IO](
    new AirlineFile(getClass.getResource("/airlines-file-sample").getPath),
    new AllowanceFile(getClass.getResource("/allowance-file-sample").getPath)
  )

  it should "parse the airlines and allowance files" in StreamAssertion {
    for {
      a <- parser.airlines
    } yield {
      a.name.value              should not be empty
      a.baggagePolicy.allowance should not be empty
    }
  }

}
