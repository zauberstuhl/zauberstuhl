package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Zauberstuhl Network"))
  }

  def statistics = Action {
    Ok(views.html.statistics("Sechat* Statistics"))
  }

}
