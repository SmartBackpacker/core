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

package com.smartbackpackerapp.common

import cats.effect.IO

object IOAssertion {
  def apply[A](ioa: IO[A]): Unit = ioa.runAsync(_ => IO.unit).unsafeRunSync()
  def when[A](predicate: Boolean)(ioa: IO[A]): Unit = {
    if (predicate) apply(ioa)
    else ()
  }
}