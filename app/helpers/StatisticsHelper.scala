package helpers

// web request for statistics.json
import scalaj.http._
import play.api.libs.json._
// use caching for web requests and calculations
import play.api.cache._
import play.api.Play.current

object StatisticsHelper {
  def get(url: String, cacheTag: String, expireCache: Int = 600): JsValue =
    Cache.getOrElse[JsValue](cacheTag, expireCache) {
      try {
        val response: HttpResponse[String] = Http(url)
          .timeout(connTimeoutMs = 5000, readTimeoutMs = 15000)
          .asString

        Json.parse(response.body)
      } catch {
        case _ : Throwable =>
          println("ERROR! Wasn't able to fetch data from " + url)
          Json.parse("{}")
      }
    }
}
