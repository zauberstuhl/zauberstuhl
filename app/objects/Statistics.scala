// Webside Source Code Zauberstuhl.de
// Copyright (C) 2016  Lukas Matt <lukas@zauberstuhl.de>
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

import play.api.libs.json._
import play.api.libs.functional.syntax._

object Statistics {
  case class DiasporaStats(halfyear: Int, monthly: Int)

  implicit val format: Format[DiasporaStats] = (
    (__ \ "active_users_halfyear").formatNullable[Int]
      .inmap[Int](_ getOrElse 0, Some(_)) and
    (__ \ "active_users_monthly").formatNullable[Int]
      .inmap[Int](_ getOrElse 0, Some(_))
  )(DiasporaStats.apply, unlift(DiasporaStats.unapply))
}
