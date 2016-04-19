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
package helpers

import play.api.libs.json.{JsValue, JsUndefined}

object Recaptcha {
  def validate(response: String): Boolean = {
    val res = Utils.fetch(Utils.confd.getString("zauberstuhl.recaptcha.url").get,
      data = Seq(
        "secret" -> Utils.confd.getString("zauberstuhl.recaptcha.secret").get,
        "response" -> response
      )
    )
    (res \ "success") match {
      case _: JsUndefined => false
      case value: JsValue => value.as[Boolean]
    }
  }
}
