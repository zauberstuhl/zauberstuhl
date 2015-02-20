package controllers

import play.api._
import play.api.mvc._

import helpers._

object Application extends Controller {
  def index = Action {
    Ok(views.html.index("Zauberstuhl Network"))
  }

  def statistics = Action {
    val url = Play.current.configuration.getString("diaspora.statisticUrl")
    val localPath = Play.current.configuration.getString("awstats.dataPath")

    Ok(views.html.statistics(
      "Sechat* Statistics",
      StatisticsHelper.get(url, localPath)))
  }
}
