package services.akka
import org.joda.time.DateMidnight
import services.HumoEvent


sealed trait BelgianTvMessage

/**
 * Sent to Master Actor
 */
case object Start extends BelgianTvMessage

case class SearchImdb(title: String, year: Option[Int]) extends BelgianTvMessage

case class SearchTomatoes(title: String, year: Option[Int]) extends BelgianTvMessage

case class FetchYelo(day: DateMidnight) extends BelgianTvMessage

case class FetchHumo(day:DateMidnight) extends BelgianTvMessage

case class FetchImdb(title: String, year: Int) extends BelgianTvMessage

case class FinishedHumo(events: List[HumoEvent])

///**
// * Sent from Search Worker to WebScrapper Worker for web scrapping of links inside tweets
// */
//case class WebScrap(tweet: Tweet) extends  Messages
//
//
///**
// * Sent from WebScrapper Worker to Search Worker with web scrapping result
// */
//case class FinishedWebScrapping(news: List[TweetNews]) extends  Messages
//
///**
// * Sent from Search Worker to Master
// */
//case class FinishedSearch(news: List[TweetNews]) extends  Messages