package helpers

import play.api.Play
import play.api.cache._
import play.api.Play.current

import javax.mail._
import javax.mail.internet._
import javax.mail.search._
import java.util.Properties

import scala.util.matching.Regex
import scala.collection.JavaConversions._

class DonationHelper() {
  def getList: List[(String, Float)] = {
    val expireCache = Play.current.configuration.getInt("zauberstuhl.cache.expire").get
    Cache.getOrElse[List[(String, Float)]]("zauberstuhl.donations", expireCache) { this.fetchData() }
  }

  private var textIsHtml: Boolean = false

  private def getText(p: Part): String = {
    if (p.isMimeType("text/*")) {
      val s = p.getContent.asInstanceOf[String]
      textIsHtml = p.isMimeType("text/html")
      return s
    }
    if (p.isMimeType("multipart/alternative")) {
      val mp = p.getContent.asInstanceOf[Multipart]
      var text: String = null
      for (i <- 0 until mp.getCount) {
        val bp = mp.getBodyPart(i)
        if (bp.isMimeType("text/plain")) {
          if (text == null) text = getText(bp)
        } else if (bp.isMimeType("text/html")) {
          val s = getText(bp)
          if (s != null) return s
        } else {
          return getText(bp)
        }
      }
      return text
    } else if (p.isMimeType("multipart/*")) {
      val mp = p.getContent.asInstanceOf[Multipart]
      for (i <- 0 until mp.getCount) {
        val s = getText(mp.getBodyPart(i))
        if (s != null) return s
      }
    }
    null
  }

  private def fetchData(): List[(String, Float)] = {
    val ms = scala.collection.mutable.MutableList[(String, Float)]()
    val p = System.getProperties()
    p.setProperty("mail.store.protocol", "imaps")
    val s = Session.getDefaultInstance(p, null)
    val store = s.getStore("imaps")
    try {
      val (zi, za, zp, zb) = this.loadCredentials
      store.connect(zi, za, zp)
      val in = store.getFolder(zb)
      in.open(Folder.READ_ONLY)
      val msgs = in.getMessages()
      for (msg <- msgs) {
        val text = this.getText(msg)
        val btc = "(\\d+\\.\\d+) (BTC)".r
        val pp = "(\\d+\\,\\d+) ([A-Z]{3})".r
        val ftr = "You made (\\d+\\.\\d+) (\\w+) in (\\w+)".r

        btc.findFirstMatchIn(text) map { case Regex.Groups(a,c)
          => ms += (this.convertPatternMatch(c,a)) }
        pp.findFirstMatchIn(text) map { case Regex.Groups(a,c)
          => ms += (this.convertPatternMatch(c,a)) }
        ftr.findFirstMatchIn(msg.getSubject) map { case Regex.Groups(a,c,d)
          => ms += (this.convertPatternMatch(c,a)) }
      }
      in.close(true)
      return ms.toList
    } catch {
      case e: Throwable => {
        println("Something went wrong in the MailHelper: " + e.getMessage())
        return List[(String, Float)]()
      }
    } finally {
      store.close()
    }
  }

  private def convertPatternMatch(currency: String, amount: String): (String, Float) =
    (currency
      .toUpperCase()
      .take(3)
    ,amount
      .replace("[^0-9,.]","")
      .replace(",",".")
      .toFloat)

  private def loadCredentials: (String, String, String, String) = {
    val c = Play.current.configuration
    (c.getString("zauberstuhl.mail.imap").get
    ,c.getString("zauberstuhl.mail.address").get
    ,c.getString("zauberstuhl.mail.password").get
    ,c.getString("zauberstuhl.mail.box").get)
  }
}
