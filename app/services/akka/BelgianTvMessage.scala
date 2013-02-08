package services.akka
import org.joda.time.DateMidnight
import models.Broadcast


sealed trait BelgianTvMessage

/**
 * Sent to Master Actor
 */
case object Start extends BelgianTvMessage

case class LinkImdb(broadcast:Broadcast) extends BelgianTvMessage

case class LinkTmdb(broadcast:Broadcast) extends BelgianTvMessage

case class LinkTomatoes(broadcast:Broadcast) extends BelgianTvMessage

case class FetchHumo(day:DateMidnight) extends BelgianTvMessage

case class FetchYelo(day:DateMidnight, events: List[Broadcast]) extends BelgianTvMessage

case class FetchBelgacom(day:DateMidnight, events: List[Broadcast]) extends BelgianTvMessage
