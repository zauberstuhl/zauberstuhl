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

import java.util.Calendar
import javax.mail.{Part, Multipart}

import scala.collection.JavaConverters._
import scala.util.matching.Regex
import scalaj.http._

import util.control.Breaks

import objects.Database.{Donation, DonationList}

object Utils {
  val confd = Play.current.configuration

  val MaxWidth = 100
  val DefaultBtcPrice = 250

  val BlockChainUrl = "https://blockchain.info/address/"
  val BlockChainParam = "?filter=2&format=json" // 1 = sent; 2 = received

  val EmptyJson = "{}" // default e.g. while fetching from url

  def buildDonationStatusInJson: String = "{\"maxWidth\":" +
    MaxWidth + ",\"width\":" +
    calculateReceivedInPercent(
      readExpenditureValues,
      DatabaseHelper.selectAllFromThisYear) +
    "}"

  def escapeHtml(s: String): String = s
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")

  def lastBtcPrice: Int = {
    val json = fetch(confd.getString("zauberstuhl.btc.url").get, "zauberstuhl.btc.conversion")
    (json \ "EUR" \ "last") match {
      case price: JsValue => price.as[Int]
      case default => {
        Logger.warn("Using default BTC value " + DefaultBtcPrice)
        DefaultBtcPrice
      }
    }
  }

  def calculateBudgetInPercent: Double = DatabaseHelper.firstEntry match {
    case Some(firstEntry) => {
      val calendarNow = Calendar.getInstance()
      val calendarThen = Calendar.getInstance()
      val sumReceived: Double = DatabaseHelper.selectAll.total
      calendarThen.setTimeInMillis(firstEntry.time * 1000L)

      val yearNow = calendarNow.get(Calendar.YEAR)
      val yearThen = calendarThen.get(Calendar.YEAR)
      val years = (yearNow - yearThen) match {
        case a if a > 0 => a + 1
        case a if a <= 0 => 1
      }
      val sumExpected = readExpenditureValues.foldLeft(0.0)(_ + _)
      val budget = (sumReceived - (sumExpected * years)).toInt

      calculateInPercent(budget, sumExpected)
    }
    case None => 0
  }

  def calculateReceivedInPercent(expens: List[Double], donations: DonationList): Double = {
    val sumExpected = expens.foldLeft(0.0)(_ + _)
    val donationSum: Double = donations.total

    calculateInPercent(donationSum, sumExpected)
  }

  def calculateInPercent(current: Double, max: Double, maxWidth: Int = MaxWidth): Double =
    if (current < max) {
      math.round((maxWidth / max) * current)
    } else {
      maxWidth
    }

  def readExpenditureValues: List[Double] = Utils.confd
    .getDoubleList("zauberstuhl.expenditures.values")
    .get.asScala.toList.map(a => a.toDouble)

  def readExpenditureReasons: List[String] = Utils.confd
    .getStringList("zauberstuhl.expenditures.reasons")
    .get.asScala.toList

  def combinedExpenditures: Map[String, Double] =
    (readExpenditureReasons zip readExpenditureValues).toMap

  def fetch(url: String, cacheTag: String = "", expireCache: Int = 600,
    data: Seq[(String, String)] = Seq()): JsValue =
  if (cacheTag.isEmpty) {
    this.fetch(url, data)
  } else {
    Cache.getOrElse[JsValue](cacheTag, expireCache) {
      this.fetch(url, data)
    }
  }

  def partText(p: Part): Option[String] = {
    if (p.isMimeType("text/*")) {
      Option(p.getContent.asInstanceOf[String])
    } else if (p.isMimeType("multipart/*")) {
      val Outer = new Breaks
      var result: Option[String] = None
      val mp = p.getContent.asInstanceOf[Multipart]
      Outer.breakable {
        for (i <- 0 until mp.getCount) {
          val bp = mp.getBodyPart(i)
          if (bp.isMimeType("text/*")) {
            result = partText(bp)
            Outer.break
          }
        }
      }
      result
    } else {
      None
    }
  }

  def convertCurrencyAmount(currency: String, amount: String): (String, Float) =
    (currency
      .toUpperCase()
      .take(3)
    ,amount
      .replace("[^0-9,.]","")
      .replace(",",".")
      .toFloat)

  private def fetch(url: String, data: Seq[(String, String)]): JsValue = try {
    val request: HttpRequest = Http(url)
    if (data.size > 0) {
      Json.parse(request.postForm(data).asString.body)
    } else {
      Json.parse(request
        // NOTE this fetches only statistic data nothing vital,
        // but in future it should accept letsencrypt
        .option(HttpOptions.allowUnsafeSSL)
        .timeout(connTimeoutMs = 5000,
          readTimeoutMs = 15000)
        .asString
        .body
      )
    }
  } catch {
    case e : Throwable => {
      Logger.error("Wasn't able to fetch data from " +
        url + ": " + e.getMessage)
      Json.parse(EmptyJson)
    }
  }
}
