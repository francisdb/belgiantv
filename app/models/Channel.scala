package models

import play.api.Logger

object Channel {

  val logger = Logger("application.channel")

  val BBC1 = "BBC 1"
  val BBC2 = "BBC 2"
  val BBC3 = "BBC 3"

  val channelFilter = List(
    "één", "Canvas", "Ketnet", "VTM", "2BE", "VIER", "VIJFtv",
    "JIM", "Ketnet", "Acht", "Ned 1", "Ned 2", "Ned 3",
    "Vitaya", BBC1, BBC2, BBC3)

  def unify(name:String):String = {

    // TODO do this in a smarter way with a hash set
    val replaced = name
      .replace("Ketnet/OP12", "Ketnet")
      .replace("vtm", "VTM")
      .replace("Vijf TV", "VijfTV")
      .replace("VIJF", "VijfTV")
      .replace("VT4", "VIER")
      .replace("Ned1", "Ned 1")
      .replace("Ned2", "Ned 2")
      .replace("Ned3", "Ned 3")
      .replace("Nederland 1", "Ned 1")
      .replace("Nederland 2", "Ned 2")
      .replace("Nederland 3", "Ned 3")
      .replace("BBC One London", BBC1)
      .replace("BBC One", BBC1)
      .replace("BBC one", BBC1)
      .replace("BBC Two London", BBC2)
      .replace("BBC Two", BBC2)
      .replace("BBC TWO", BBC2)
      .replace("BC Three / CBBC", BBC3)
      .replace(" hd", "")
      .replace(" HD", "")
    //      if (!channelFilter.contains(replaced)){
    //        logger.warn(s"Unhandled channel name: $replaced")
    //      }
    replaced
  }

}
