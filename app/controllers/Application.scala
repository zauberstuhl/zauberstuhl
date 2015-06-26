package controllers

import play.api._
import play.api.mvc._

import scala.collection.JavaConverters._

import helpers._

object Application extends Controller {
  val c = Play.current.configuration

  def index = Action {
    Ok(views.html.index("Zauberstuhl Network"))
  }

  def statistics = Action {
    val url = c.getString("zauberstuhl.diaspora.url").get
    val localPath = c.getString("zauberstuhl.awstats.path").get
    val expire = c.getInt("zauberstuhl.cache.expire").get

    Ok(views.html.statistics(
      "Sechat* Statistics",
      StatisticsHelper.get(url, localPath, expire)))
  }

  def donate(json: Boolean) = Action {
    val values = c.getDoubleList("zauberstuhl.expenditures.values")
      .getOrElse(null)
    val reasons = c.getStringList("zauberstuhl.expenditures.reasons")
      .getOrElse(null)
    val e = (reasons.asScala zip values.asScala).toMap
    val t = (new DonationHelper).getList

    if (json) Ok(
      Global.buildDonateJSON(e, t)
    ) else Ok(views.html.donate("Donate the Network", e, t))
  }
}
