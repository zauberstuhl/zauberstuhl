package controllers

import play.api._
import play.api.mvc._

import scala.collection.JavaConverters._

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

  def donate = Action {
    val values = Play.current.configuration
      .getDoubleList("zauberstuhl.expenditures.values")
      .getOrElse(null)
    val reasons = Play.current.configuration
      .getStringList("zauberstuhl.expenditures.reasons")
      .getOrElse(null)
    val expenditures = (reasons.asScala zip values.asScala).toMap
    val transactions = PayPalHelper.getTransactions

    Ok(views.html.donate("Donate the Network",
      expenditures, transactions
    ))
  }

  def ipn = Action { request =>
    PayPalHelper.validateAndSaveTransaction(request)
    Ok
  }
}
