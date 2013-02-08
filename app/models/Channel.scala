package models

import play.api.Logger

object Channel {

  val logger = Logger("application.channel")

  val channelFilter = List("één", "Canvas", "Ketnet", "VTM", "2BE", "VIER", "VIJFtv", "JIM", "Ketnet", "Acht", "Ned 1", "Ned 2", "Ned 3", "Vitaya", "BBC 1", "BBC 2", "BBC 3")

  def unify(name:String):String = {

    // TODO do this in a smarter way with a hash set

    if (name == null){
      null
    }else{
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
        .replace("BBC One London", "BBC 1")
        .replace("BBC Two London", "BBC 2")
//      if (!channelFilter.contains(replaced)){
//        logger.warn(s"Unhandled channel name: $replaced")
//      }
      replaced
    }
  }

}
