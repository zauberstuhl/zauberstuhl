package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

import scala.collection.JavaConverters._

import java.util.Calendar

import helpers._

object Application extends Controller {
  val c = Play.current.configuration

  def index = Action {
    Ok(views.html.index("Contact"))
  }

  def statistics = Action {
    val sechat_key = "zauberstuhl.stats.sechat"
    val jd_key = "zauberstuhl.stats.joindiaspora"
    val sechat_url = c.getString(sechat_key).get
    val jd_url = c.getString(jd_key).get
    val expire = c.getInt("zauberstuhl.cache.expire").get

    val sechat_json = StatisticsHelper.get(sechat_url, sechat_key, expire)
    val joindiaspora_json = StatisticsHelper.get(jd_url, jd_key, expire)

    Ok(views.html.statistics("Statistics",
      Json.obj( "sechat" -> sechat_json, "joindiaspora" -> joindiaspora_json)))
  }

  def donate(json: Boolean) = Action {
    val values = c.getDoubleList("zauberstuhl.expenditures.values")
      .getOrElse(null)
    val reasons = c.getStringList("zauberstuhl.expenditures.reasons")
      .getOrElse(null)
    val donationReasons = (reasons.asScala zip values.asScala).toMap
    val donations = (new DonationHelper).getList

    val title = "Donations for " + Calendar.getInstance().get(Calendar.YEAR)

    if (json) Ok(
      Global.buildDonateJSON(donationReasons, donations)
    ) else Ok(views.html.donate(title, donationReasons, donations))
  }
}
