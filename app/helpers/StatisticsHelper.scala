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
import scala.concurrent.duration._
import play.api.Play.current

object StatisticsHelper {
  def get(url: Option[String], localPath: Option[String]): Map[String, JsValue] =
    Cache.getOrElse[Map[String, JsValue]]("zauberstuhl.statistics") {
      println("No cache")
      var awstats = ListBuffer[Map[String, String]]()
      val response: HttpResponse[String] = Http(url.get).asString

      new File(localPath.get).listFiles.foreach { file =>
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
      val res = Map(
        "awstats" -> Json.toJson(awstats),
        "diaspora" -> Json.parse(response.body)
      )
      // set duration for caching
      Cache.set("zauberstuhl.statistics", res, 1.minutes)
      res
    }
}
