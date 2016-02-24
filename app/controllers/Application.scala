package controllers

import play.api._
import play.api.mvc._

import scala.collection.JavaConverters._

import java.util.Calendar

import helpers._

object Application extends Controller {
  val c = Play.current.configuration

  def index = Action {
    Ok(views.html.index("Contact"))
  }

  def statistics = Action {
    val url = c.getString("zauberstuhl.diaspora.url").get
    val expire = c.getInt("zauberstuhl.cache.expire").get

    Ok(views.html.statistics("Statistics",
      StatisticsHelper.get(url, "zauberstuhl.statistics", expire)))
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
