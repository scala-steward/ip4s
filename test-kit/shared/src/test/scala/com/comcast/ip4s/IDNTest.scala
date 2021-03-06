/*
 * Copyright 2018 Comcast Cable Communications Management, LLC
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

package com.comcast.ip4s

import Arbitraries._

class IDNTest extends BaseTestSuite {
  "IDN" should {
    "support any hostname" in {
      forAll { (h: Hostname) =>
        val i = IDN.fromHostname(h)
        val representable = h.labels.forall(l => !l.toString.toLowerCase.startsWith("xn--"))
        if (representable) {
          i.hostname shouldBe h
          IDN(h.toString) shouldBe Some(i)
        }
      }
    }

    "roundtrip through string" in {
      forAll { (i: IDN) => IDN(i.toString) shouldBe Some(i) }
    }

    "allow access to labels" in {
      forAll { (i: IDN) => IDN(i.labels.toList.mkString(".")).map(_.labels) shouldBe Some(i.labels) }
    }

    "require overall ascii length be less than 254 chars" in {
      forAll { (i: IDN) =>
        val istr = i.toString
        val i2 = istr + "." + istr
        val expected = if (i.hostname.toString.length > (253 / 2)) None else Some(IDN(i2).get)
        IDN(i2) shouldBe expected
      }
    }

    "require labels be less than 64 ascii chars" in {
      forAll { (i: IDN) =>
        val str = i.toString
        val suffix = new String(Array.fill(63)(str.last))
        val tooLong = str + suffix
        IDN(tooLong) shouldBe None
      }
    }

    "disallow labels that end in a dash" in {
      forAll { (i: IDN) =>
        val str = i.toString
        // Note: simply appending a dash to final label doesn't guarantee the ASCII encoded label ends with a dash
        val disallowed = str + ".a-"
        IDN(disallowed) shouldBe None
      }
    }

    "disallow labels that start with a dash" in {
      forAll { (i: IDN) =>
        val str = i.toString
        val disallowed = "-a." + str
        IDN(disallowed) shouldBe None
      }
    }

    "support normalization" in {
      forAll { (i: IDN) => i.normalized shouldBe IDN(i.labels.map(_.toString.toLowerCase).toList.mkString(".")).get }
    }
  }
}
