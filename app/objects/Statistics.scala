package objects

import play.api.libs.json._
import play.api.libs.functional.syntax._

object Statistics {
  case class DiasporaStats(halfyear: Int, monthly: Int)

  implicit val format: Format[DiasporaStats] = (
    (__ \ "active_users_halfyear").formatNullable[Int]
      .inmap[Int](_ getOrElse 0, Some(_)) and
    (__ \ "active_users_monthly").formatNullable[Int]
      .inmap[Int](_ getOrElse 0, Some(_))
  )(DiasporaStats.apply, unlift(DiasporaStats.unapply))
}
