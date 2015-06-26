package helpers

import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future

object Global extends GlobalSettings {
  val maxWidth = 100

  def escapeHtml(s: String): String = s
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")

  def getProgressWidth(e: Map[String, java.lang.Double], t: List[(String, Float)]): Int = {
    val btcc = Play.current.configuration.getInt("zauberstuhl.btc.conversion").getOrElse(1)
    val ec: Double = e.foldLeft(0.0)(_+_._2)
    var tc: Float = 0;
    for ((k, v) <- t) {
      if (k == "BTC") tc += v * btcc else v
    }
    if (tc.abs > ec.abs) return this.maxWidth
    (tc / (ec / this.maxWidth)).toInt
  }

  def buildDonateJSON(e: Map[String, java.lang.Double], t: List[(String, Float)]): String =
    """{"maxWidth":"""+this.maxWidth+""","width":"""+this.getProgressWidth(e, t)+"""}"""

  override def onError(req: RequestHeader, ex: Throwable) = {
    Future.successful(InternalServerError(views.html.error(
      "500 Internal Server Error",
      "Outsch .. What have you done :?")))
  }

  override def onBadRequest(req: RequestHeader, error: String) = {
    Future.successful(NotFound(views.html.error(
      "400 Bad Request",
      "The request could not be understood by the server!")))
  }

  override def onHandlerNotFound(req: RequestHeader) = {
    Future.successful(NotFound(views.html.error(
      "404 Not Found",
      "The requested URL was not found on the server!")))
  }
}
