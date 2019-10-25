// Webside Source Code Zauberstuhl.de
// Copyright (C) 2016-2019  Lukas Matt <lukas@matt.wf>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
package objects

object Database {
  case class Project(title: String, body: String)

  case class ProjectList(list: List[Project])

  case class Donation(received: Double, currency: String, provider: String, time: Int) {
    require(received >= 0, "received cannot be negative")
    require(provider != null, "provider cannot be null")
    require(currency != null, "currency cannot be null")
    require(time > 1000000000, "time should be a unix timestamp")

    def received(roundTo: Int): Double =
      (("%." + roundTo + "f").format(received)).toDouble

    override def toString: String =
      "received -> " + received.toString +
      ", currency -> " + currency +
      ", provider -> " + provider +
      ", time -> " + time.toString
  }

  case class DonationList(list: List[Donation]) {
    def total: Double = list.foldLeft(0.0) {
      (a: Double, b: Donation) => (b.received + a)
    }

    override def toString: String = list.foldLeft("") {
      (a: String, b: Donation) => a + "\n" + b.toString
    }
  }
}
