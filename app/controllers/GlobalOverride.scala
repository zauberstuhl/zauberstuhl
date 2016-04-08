package helpers

import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future

object GlobalOverride extends GlobalSettings {
  override def onStart(app: Application) {
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
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
