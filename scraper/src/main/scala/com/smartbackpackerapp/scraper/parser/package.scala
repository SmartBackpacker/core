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

package com.smartbackpackerapp.scraper

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
