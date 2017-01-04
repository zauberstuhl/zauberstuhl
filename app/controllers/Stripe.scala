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

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import com.stripe.exception.StripeException
import com.stripe.model._
import com.stripe.net.RequestOptions

import java.util.HashMap

import helpers._
import objects.Provider._
import objects.Database._

object Stripe extends Controller {
  val provider = StripeProvider()

  def oneTimePayment = Action.async(parse.urlFormEncoded) { request =>
    val amount: Int = (request.body("amount").head).toInt
    val token: String = request.body("stripeToken").head
    com.stripe.Stripe.apiKey = Utils.confd("stripe.apiKey")

    try {
      // Create a Customer
      val customerParams = new HashMap[String, Object]()
      customerParams.put("source", token)
      //customerParams.put("description", comment)
      val customer = Customer.create(customerParams)

      // Charge the Customer instead of the card
      val cents: java.lang.Integer = amount * 100
      val chargeParams = new HashMap[String, Object]()
      chargeParams.put("amount", cents)
      chargeParams.put("currency", "eur")
      chargeParams.put("customer", customer.getId())
      Charge.create(chargeParams)

      // safe into database
      val donation = Donation(amount.toFloat, "EUR",
        provider.name, (System.currentTimeMillis / 1000).toInt)
      DatabaseHelper.insert(donation)

      Future(Ok(views.html.stripe(request, "zauberstuhl")))
    } catch {
      case e: StripeException => {
        Logger.error(e.getMessage)
        Future(BadRequest(e.getMessage))
      }
    }
  }
}
