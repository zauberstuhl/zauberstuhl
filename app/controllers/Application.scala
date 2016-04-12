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

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import helpers._
import objects.Database.Donation

object Application extends Controller {
  val sechatKey = "zauberstuhl.stats.sechat"
  val jdKey = "zauberstuhl.stats.joindiaspora"

  val values = Utils.confd.getDoubleList(
    "zauberstuhl.expenditures.values").get
  val reasons = Utils.confd.getStringList(
    "zauberstuhl.expenditures.reasons").get

  val sechatUrl = Utils.confd.getString(sechatKey).get
  val jdUrl = Utils.confd.getString(jdKey).get
  val expire = Utils.confd.getInt("zauberstuhl.cache.expire").get

  def index = Action.async { implicit request =>
    for {
      donationReasons <- Future { (reasons.asScala zip values.asScala).toMap }
      donations <- Future { DatabaseHelper.selectAllFromThisYear }
      statistics <- Future {
        val sechatJson = Utils.fetch(sechatUrl, sechatKey, expire)
        val jdJson = Utils.fetch(jdUrl, jdKey, expire)

        Json.obj("sechat" -> sechatJson, "jd" -> jdJson)
      }
    } yield Ok(views.html.index(request,
      "zauberstuhl",
      donationReasons,
      donations,
      statistics))
  }

  def donate(json: Boolean) = Action { implicit request =>
    val donationReasons = (reasons.asScala zip values.asScala).toMap
    val donations: List[Donation] = DatabaseHelper.selectAllFromThisYear

    if (json) {
      Ok(Utils.buildDonateJSON(donationReasons, donations))
    } else {
      // this is required for backward compatibility
      Redirect(routes.Application.index + "#donate")
    }
  }
}
