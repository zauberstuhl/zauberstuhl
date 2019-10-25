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
package helpers

import play.api.Logger
import play.api.libs.json.{JsValue, JsUndefined}

object Recaptcha {
  def validate(response: String): Boolean = {
    Logger.debug("Try validating reCaptcha response: " + response)
    val res = Utils.fetch(Utils.confd("recaptcha.url"),
      data = Seq(
        "secret" -> Utils.confd("recaptcha.secret"),
        "response" -> response
      )
    )
    (res \ "success") match {
      case _: JsUndefined => false
      case value: JsValue => value.as[Boolean]
    }
  }
}
