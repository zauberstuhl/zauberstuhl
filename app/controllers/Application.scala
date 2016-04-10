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
package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

import scala.collection.JavaConverters._

import java.util.Calendar

import helpers._

object Application extends Controller {
  def index = Action {
    Ok(views.html.index("Contact"))
  }

  def statistics = Action {
    val sechat_key = "zauberstuhl.stats.sechat"
    val jd_key = "zauberstuhl.stats.joindiaspora"
    val sechat_url = Utils.confd.getString(sechat_key).get
    val jd_url = Utils.confd.getString(jd_key).get
    val expire = Utils.confd.getInt("zauberstuhl.cache.expire").get

    val sechat_json = Utils.fetch(sechat_url, sechat_key, expire)
    val joindiaspora_json = Utils.fetch(jd_url, jd_key, expire)

    Ok(views.html.statistics("Statistics",
      Json.obj( "sechat" -> sechat_json, "joindiaspora" -> joindiaspora_json)))
  }

  def donate(json: Boolean) = Action {
    val values = Utils.confd.getDoubleList(
      "zauberstuhl.expenditures.values").get
    val reasons = Utils.confd.getStringList(
      "zauberstuhl.expenditures.reasons").get

    val donationReasons = (reasons.asScala zip values.asScala).toMap
    val donations: List[Donation] = DatabaseHelper.selectAllFromThisYear

    val title = "Donations for " + Calendar.getInstance().get(Calendar.YEAR)

    if (json) {
      Ok(Utils.buildDonateJSON(donationReasons, donations))
    } else {
      Ok(views.html.donate(title, donationReasons, donations))
    }
  }
}
