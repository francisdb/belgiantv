package models

import play.api.Logger

object Channel {

  val logger = Logger("application.channel")

  val BBC1 = "BBC 1"
  val BBC2 = "BBC 2"
  val BBC3 = "BBC 3"

  val NPO1 = "NPO 1"
  val NPO2 = "NPO 2"
  val NPO3 = "NPO 3"

  val channelFilter = Set(
    "één",
    "Canvas",
    "Ketnet",
    "VTM",
    "2BE",
    "VIER",
    "VIJF",
    "ZES",
    "CAZ",
    "JIM",
    "Ketnet",
    "Acht",
    NPO1,
    NPO2,
    NPO3,
    "Vitaya",
    "VTM KIDS",
    "VTM KIDS Jr",
    "Q2",
    BBC1,
    BBC2,
    BBC3,
    "Studio 100 TV",
    "Disney Channel VL"
  )

  def unify(name: String): String = {
    // TODO do this in a smarter way with a hash set
    val replaced = name
      .replace("Ketnet/OP12", "Ketnet")
      .replace("vtmKzoom", "VTM KIDS")
      .replace("VTM Kzoom", "VTM KIDS")
      .replace("vtm", "VTM")
      .replace("Vijf TV", "VIJF")
      .replace("VijfTV", "VIJF")
      .replace("VT4", "VIER")
      .replace("Ned1", "Ned 1")
      .replace("Ned2", "Ned 2")
      .replace("Ned3", "Ned 3")
      .replace("Nederland 1", NPO1)
      .replace("Nederland 2", NPO2)
      .replace("Nederland 3", NPO3)
      .replace("Ned 1", NPO1)
      .replace("Ned 2", NPO2)
      .replace("Ned 3", NPO3)
      .replace("NPO-1", NPO1)
      .replace("NPO-2", NPO2)
      .replace("NPO-3", NPO3)
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
