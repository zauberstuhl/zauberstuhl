// Webside Source Code Zauberstuhl.de
// Copyright (C) 2016  Lukas Matt <lukas@zauberstuhl.de>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Results._

import akka.actor.Actor
import akka.actor.Props
import scala.concurrent.Future
import scala.concurrent.duration._

import helpers._
import workers._

object GlobalOverride extends GlobalSettings {
  override def onStart(app: Application) {
    val system = akka.actor.ActorSystem("system")
    import system.dispatcher
    val actor = system.actorOf(
      Props.create(classOf[UpdateActor])
    )
    // workers.BlockChainActor
    system.scheduler.schedule(0.seconds,
      30.minutes, actor, BlockChainProvider())
    system.scheduler.schedule(60.seconds,
      30.minutes, actor, EmailProvider())
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
