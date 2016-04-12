package objects

object Database {
  case class Donation(received: Double, currency: String, provider: String, time: Int) {
    require(received >= 0, "received cannot be negative")
    require(provider != null, "provider cannot be null")
    require(currency != null, "currency cannot be null")
    require(time > 1000000000, "time should be a unix timestamp")

    def received(roundTo: Int): Double =
      (("%." + roundTo + "f").format(received)).toDouble

    override def toString: String =
      "received -> " + received.toString +
      ", currency -> " + currency +
      ", provider -> " + provider +
      ", time -> " + time.toString
  }
}
