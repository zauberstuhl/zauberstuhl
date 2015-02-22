package helpers

import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future

object Global extends GlobalSettings {
  def escapeHtml(s: String): String = s
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")

  def getProgressWidth(expenditures: Map[String, java.lang.Double], transactions: Map[Int, Map[String, Any]]): Int = {
    val expendituresCnt: Double = expenditures.foldLeft(0.0)(_+_._2)
    val transactionCnt: Double = transactions.map {
      case (_, b) => b.map {
        case (k: String, v: Double) => v
        case _ => 0.0 // return zero if something else
      }
    }.foldLeft(0.0)(_+_.head)
    if (transactionCnt >= expendituresCnt) return 100
    (transactionCnt / (expendituresCnt / 100.0)).toInt
  }

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
