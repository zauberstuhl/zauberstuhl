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
package workers

import play.api.Play
import play.api.Logger
import play.api.libs.json.{JsValue, Json}

import akka.actor.Actor
import akka.actor.Props
import scalaj.http._
import scala.concurrent.duration._
import scala.util.matching.Regex

import javax.mail.{Session, Folder, Message}

import helpers._
import objects.Database.Donation
import objects.Statistics.DiasporaStats
import objects.Provider.{Provider, _}

class UpdateActor extends Actor {
  val SATOSHIBASE: Int = 10
  val SATOSHIEXPR: Int = -8

  val confd = Play.current.configuration

  def receive = {
    case p: BlockChainProvider => this.blockChainProvider(p)
    case p: EmailProvider => this.emailProvider(p)
    case _ => // nothing todo
  }

  private def blockChainProvider(provider: Provider) {
    Logger.info("[" + provider.name + "] Startup..")
    val address = confd.getString("zauberstuhl.btc.address").get
    val json = Utils.fetch(
      Utils.BlockChainUrl + address + Utils.BlockChainParam
    )
    val preTime: Int = DatabaseHelper.lastEntry(provider) match {
      case Some(entry) => entry.time
      case None => 0
    }
    // enter raw data and check if txs (transactions) exist
    (json \ "txs") match { case txs: JsValue =>
      txs.as[Array[JsValue]].foreach { txsEntries => txsEntries match {
        case txsEntry: JsValue =>
          // if it exists check the time first
          (txsEntry \ "time") match {
            // if it is greater then the last entry procced
            case time: JsValue => if (time.as[Int] > preTime) {
                // lets try fetching the out array which includes all satoshi values and btc addresses
                (txsEntry \ "out") match {
                  case out: JsValue =>
                    out.as[Array[JsValue]].foreach { outEntries =>
                    outEntries match {
                      // if the addr matches our own we want to insert the donation into our db
                      case outEntry: JsValue => if ((outEntry \ "addr").as[String] == address) {
                        // the donation is in satoshi (btc price * 10^(-8))
                        val satoshi: Int = (outEntry \ "value").as[Int]
                        // convert from satoshi to btc and multiply it by the current stock price
                        val received: Double =
                          (Utils.lastBtcPrice * scala.math.pow(SATOSHIBASE, SATOSHIEXPR)) * satoshi
                        // finally insert everything into db
                        DatabaseHelper.insert(
                          Donation(received, "EUR", provider.name, time.as[Int])
                        )
                      }
                    }
                  }
                }
              } else {
                Logger.debug("[" + provider.name + "] Donation already known! Skipping..")
              }
          }
      }}
    }
    Logger.info("[" + provider.name + "] Finished")
  }

  private def emailProvider(provider: Provider) {
    Logger.info("[" + provider.name + "] Startup..")
    val props = System.getProperties()
    props.setProperty("mail.store.protocol", "imaps")
    val store = Session.getDefaultInstance(props).getStore("imaps")
    try {
      store.connect(
        confd.getString("zauberstuhl.mail.imap").get,
        confd.getString("zauberstuhl.mail.address").get,
        confd.getString("zauberstuhl.mail.password").get
      )
      val in = store.getFolder(confd.getString("zauberstuhl.mail.box").get)
      in.open(Folder.READ_ONLY)
      val preTime: Int = DatabaseHelper.lastEntry(provider) match {
        case Some(entry) => entry.time
        case None => 0
      }
      for (msg <- in.getMessages()) {
        val time: Int = (msg.getReceivedDate.getTime / 1000L).toInt
        if (time > preTime) {
          if (fromIsValid(msg)) {
            val regex = "(\\d+\\,\\d+) ([A-Z]{3})".r
            val text: String = Utils.partText(msg).getOrElse("")
            regex.findFirstMatchIn(text) map {
              case Regex.Groups(rawAmount, rawCurrency) => {
                val (currency: String, amount: Float) =
                  Utils.convertCurrencyAmount(rawCurrency, rawAmount)
                  val donation = Donation(amount, currency, provider.name, time)
                  DatabaseHelper.insert(donation)
              }
            }
          }
        } else {
          Logger.debug("[" + provider.name + "] Donation already known! Skipping..")
        }
      }
      in.close(true)
    } catch {
      case e: Throwable => Logger.error("[" + provider.name + "] " + e.getMessage())
    } finally {
      store.close()
    }
    Logger.info("[" + provider.name + "] Finished")
  }

  private def fromIsValid(msg: Message): Boolean = {
    val paypal = "@paypal.de"
    val spreadshirt = "@spreadshirt.net"
    // custom will allow the further process of draft emails
    val custom = "@zauberstuhl.de"
    val from: String = msg.getFrom.mkString(",")
    from match {
      case str if str contains paypal => true
      case str if str contains spreadshirt => true
      case str if str contains custom => true
      case _ => false
    }
  }
}
