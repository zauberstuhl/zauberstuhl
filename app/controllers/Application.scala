package controllers

import play.api._
import play.api.mvc._

import helpers._

object Application extends Controller {
  def index = Action {
    Ok(views.html.index("Zauberstuhl Network"))
  }

  def statistics = Action {
    val url = Play.current.configuration.getString("zauberstuhl.diaspora.url").get
    val localPath = Play.current.configuration.getString("zauberstuhl.awstats.path").get
    val expire = Play.current.configuration.getInt("zauberstuhl.cache.expire").get

    Ok(views.html.statistics(
      "Sechat* Statistics",
      StatisticsHelper.get(url, localPath, expire)))
  }
}
