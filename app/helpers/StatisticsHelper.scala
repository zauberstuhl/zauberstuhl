package helpers

// web request for statistics.json
import scalaj.http._
import play.api.libs.json._
// local request for awstats
import java.io.File
import scala.io.Source
import scala.collection.mutable.ListBuffer
// use caching for web requests and calculations
import play.api.cache._
import play.api.Play.current

object StatisticsHelper {
  def get(url: String, localPath: String, expireCache: Int = 600): Map[String, JsValue] =
    Cache.getOrElse[Map[String, JsValue]]("zauberstuhl.statistics", expireCache) {
      var awstats = ListBuffer[Map[String, String]]()
      val response: HttpResponse[String] = Http(url)
        .option(HttpOptions.allowUnsafeSSL)
        .asString

      try {
        new File(localPath).listFiles.foreach { file =>
          val date = """^.*\/awstats(\d{2})(\d{4}).txt$""".r
          val entry = scala.collection.mutable.Map[String, String]()
          file.getAbsolutePath match {
            case date(month, year) => {
              entry += ("period" -> s"$year-$month-00")
              for (line <- Source.fromFile(file).getLines()) {
                val total = """^TotalVisits\s(\d+).*""".r
                val unique = """^TotalUnique\s(\d+).*""".r
                line match {
                  case total(cnt) => entry += "all" -> cnt
                  case unique(cnt) => entry += "unique" -> cnt
                  case _ =>
                }
              }
            }
            case _ =>
          }
          if (entry.size == 3) awstats += entry.toMap
        }
      } catch {
        case npe: NullPointerException => println(s"Directory does not exist: $localPath")
      }
      Map(
        "awstats" -> Json.toJson(awstats),
        "diaspora" -> Json.parse(response.body)
      )
    }
}
