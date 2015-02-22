package helpers

import play.api._
import play.api.mvc._
import scalaj.http._
// sqlite database
import anorm._
import play.api.db.DB
// async tasks
import scala.concurrent._
import ExecutionContext.Implicits.global

object PayPalHelper {
  def getTransactions: Map[Int, Map[String, Any]] = {
    import play.api.Play.current
    DB.withConnection { implicit c =>
      SQL("SELECT ROWID, currency, gross, fee FROM PAYPAL;")().map(
        row => row[Int]("ROWID") -> Map(
          "donation" -> (row[Double]("gross") - row[Double]("fee")),
          "currency" -> row[String]("currency"))
      ).toMap
    }
  }

  def validateAndSaveTransaction(req: Request[AnyContent]) = future {
    if (isValidContent(req)) {
      import play.api.Play.current
      DB.withConnection { implicit c =>
        val dataMap = req.body.asFormUrlEncoded
        SQL(s"""INSERT INTO PAYPAL (
        |  payer_id, first_name, last_name, email, gross, fee, currency
        |) VALUES (
        |  '${dataMap.get("payer_id").head}', '${dataMap.get("first_name").head}',
        |  '${dataMap.get("last_name").head}', '${dataMap.get("payer_email").head}',
        |  ${dataMap.get("mc_gross").head}, ${dataMap.get("mc_fee").head},
        |  '${dataMap.get("mc_currency").head}'
        |);""".stripMargin).execute
      }
    }
  }

  private def buildValidateString(req: Request[AnyContent]): String = {
    var validateString = "cmd=_notify-validate"
    req.body.asFormUrlEncoded.foreach {
      case (map: Map[String, Seq[String]] ) => map.foreach {
        case (key, seq) => validateString += s"&$key=${seq.mkString}"
      }
    }
    validateString
  }

  private def isValidContent(req: Request[AnyContent]): Boolean = {
    val validateString = buildValidateString(req)
    val url = Play.current.configuration.getString("zauberstuhl.paypal.url").getOrElse {
      return false
    }
    val result: HttpResponse[String] = Http(url)
      .option(HttpOptions.allowUnsafeSSL)
      .option(HttpOptions.followRedirects(false))
      .header("Content-Type", "application/x-www-form-urlencoded")
      .header("Content-Length", s"${validateString.length}")
      .postData(validateString)
      .asString

    (result.isSuccess && result.body == "VERIFIED")
  }
}
