package helpers

// web request for statistics.json
import scalaj.http._
import play.api.libs.json._
// use caching for web requests and calculations
import play.api.cache._
import play.api.Play.current

object StatisticsHelper {
  def get(url: String, expireCache: Int = 600): JsValue =
    Cache.getOrElse[JsValue]("zauberstuhl.statistics", expireCache) {
      val response: HttpResponse[String] = Http(url)
        //.option(HttpOptions.allowUnsafeSSL)
        .asString
      Json.parse(response.body)
    }
}
