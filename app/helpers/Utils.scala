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
package helpers

import play.api.{Play, Logger}
import play.api.libs.json.{JsValue, Json}
import play.api.cache._
import play.api.Play.current

import javax.mail.{Part, Multipart}
import scala.util.matching.Regex
import scalaj.http._

import util.control.Breaks

trait Provider {
  def name: String =
    "(Provider|helpers.)".r.replaceAllIn(getClass.getName, "")
}
case class BlockChainProvider() extends Provider
case class EmailProvider() extends Provider

object Utils {
  val confd = Play.current.configuration

  val MaxWidth = 100
  val DefaultBtcPrice = 250

  val BlockChainUrl = "https://blockchain.info/address/"
  val BlockChainParam = "?filter=2&format=json" // 1 = sent; 2 = received

  def buildDonateJSON(reasons: Map[String, java.lang.Double], donations: List[Donation]): String =
    """{"maxWidth":""" + MaxWidth + ""","width":""" + this.getProgressWidth(reasons, donations) + """}"""

  def escapeHtml(s: String): String = s
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")

  def lastBtcPrice: Int = {
    val json = fetch(confd.getString("zauberstuhl.btc.url").get, "zauberstuhl.btc.conversion")
    (json \ "EUR" \ "last") match {
      case price: JsValue => price.as[Int]
      case default => {
        println("[WARN] Using default BTC value " + DefaultBtcPrice)
        DefaultBtcPrice
      }
    }
  }

  def getProgressWidth(reasons: Map[String, java.lang.Double], donations: List[Donation]): Int = {
    val reasonSum: Double = reasons.foldLeft(0.0)(_ + _._2)
    val donationSum: Double = donations.foldLeft(0.0) {
      (a: Double, b: Donation) => (b.received + a)
    }
    if (donationSum > reasonSum) return MaxWidth
    (donationSum / (reasonSum / MaxWidth)).toInt
  }

  def fetch(url: String,
    cacheTag: String = null,
    expireCache: Int = 600): JsValue = if (cacheTag == null) {
    this._fetch(url)
  } else {
    Cache.getOrElse[JsValue](cacheTag, expireCache) {
      this._fetch(url)
    }
  }

  def partText(p: Part): Option[String] = {
    val Outer = new Breaks
    var res: Option[String] = None
    if (p.isMimeType("text/*")) {
      Option(p.getContent.asInstanceOf[String])
    } else if (p.isMimeType("multipart/alternative")) {
      val mp = p.getContent.asInstanceOf[Multipart]
      Outer.breakable {
        for (i <- 0 until mp.getCount) {
          val bp = mp.getBodyPart(i)
          if (bp.isMimeType("text/plain") && res == None) {
            res = partText(bp)
            Outer.break
          } else if (bp.isMimeType("text/html")) {
            partText(bp) match {
              case Some(plain) =>
                res = Option(plain)
                Outer.break
              case None => //
            }
          } else {
            res = partText(bp)
            Outer.break
          }
        }
      }
    } else if (p.isMimeType("multipart/*")) {
      val mp = p.getContent.asInstanceOf[Multipart]
      Outer.breakable {
        for (i <- 0 until mp.getCount) {
          partText(mp.getBodyPart(i)) match {
            case Some(plain) =>
              res = Option(plain)
              Outer.break
            case None => //
          }
        }
      }
    }
    res
  }

  def convertCurrencyAmount(currency: String, amount: String): (String, Float) =
    (currency
      .toUpperCase()
      .take(3)
    ,amount
      .replace("[^0-9,.]","")
      .replace(",",".")
      .toFloat)

  private def _fetch(url: String): JsValue = try {
    val response: HttpResponse[String] = Http(url)
      .timeout(connTimeoutMs = 5000,
        readTimeoutMs = 15000)
      .asString
    Json.parse(response.body)
  } catch {
    case _ : Throwable => {
      Logger.error("Wasn't able to fetch data from " + url)
      Json.parse("{}")
    }
  }
}
