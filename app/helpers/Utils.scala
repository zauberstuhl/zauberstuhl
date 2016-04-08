package helpers

import play.api._

object Utils {
  val maxWidth = 100

  def escapeHtml(s: String): String = s
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")

  def getProgressWidth(e: Map[String, java.lang.Double], t: List[(String, Float)]): Int = {
    var btcc: Int = 220;
    val c = Play.current.configuration
    val conv = StatisticsHelper.get(
      c.getString("zauberstuhl.btc.url").get,
      "zauberstuhl.btc.conversion")
    try {
      btcc = (conv \ "EUR" \ "last").as[Int]
    } catch {
      case _ : Throwable =>
        println("Wasn't able to parse BTC conversion. Using default value " + btcc)
    }

    val ec: Double = e.foldLeft(0.0)(_+_._2)
    var tc: Float = 0;
    for ((k, v) <- t) {
      if (k == "BTC") tc += v * btcc else tc += v
    }
    if (tc.abs > ec.abs) return this.maxWidth
    (tc / (ec / this.maxWidth)).toInt
  }

  def buildDonateJSON(e: Map[String, java.lang.Double], t: List[(String, Float)]): String =
    """{"maxWidth":"""+this.maxWidth+""","width":"""+this.getProgressWidth(e, t)+"""}"""
}
