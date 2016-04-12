package objects

object Provider {
  trait Provider {
    def name: String = getClass.getName
  }
  case class BlockChainProvider() extends Provider
  case class EmailProvider() extends Provider
}
