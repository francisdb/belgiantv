package services.actors
import java.time.LocalDate

import models.Broadcast
import models.helper.TomatoesMovie
import reactivemongo.bson.BSONObjectID
import services.HumoEvent

import scala.util.Try


sealed trait BelgianTvMessage

/**
 * Sent to Master Actor
 */
case object Start extends BelgianTvMessage

case object StartTomatoes extends BelgianTvMessage

case class LinkImdb(broadcast:Broadcast) extends BelgianTvMessage

case class LinkTmdb(broadcast:Broadcast) extends BelgianTvMessage

case class LinkTomatoes(broadcast:Broadcast) extends BelgianTvMessage

// TODO how do I avoid sending the broadcastId?
case class FetchTomatoes(title:String, year: Option[Int], broadcastId: BSONObjectID) extends BelgianTvMessage
case class FetchTomatoesResult(tomatoesMovie:TomatoesMovie, broadcastId: BSONObjectID) extends BelgianTvMessage

case class FetchHumo(day:LocalDate) extends BelgianTvMessage
case class FetchHumoResult(day:LocalDate, events:Try[Seq[HumoEvent]]) extends BelgianTvMessage

case class FetchYelo(day:LocalDate, events: Seq[Broadcast]) extends BelgianTvMessage

case class FetchBelgacom(day:LocalDate, events: Seq[Broadcast]) extends BelgianTvMessage
