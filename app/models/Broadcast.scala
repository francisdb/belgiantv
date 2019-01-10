package models

import java.time.Instant
import java.time.format.DateTimeFormatter

import global.Globals
import reactivemongo.bson._

import Broadcast._

case class Broadcast(
  id: Option[BSONObjectID],
  name: String,
  channel: String,
  datetime: Instant,
  year: Option[Int] = None,
  humoId: Option[String] = None,
  humoUrl: Option[String] = None,
  yeloId: Option[String] = None,
  yeloUrl: Option[String] = None,
  belgacomId: Option[String] = None,
  belgacomUrl: Option[String] = None,
  imdbId: Option[String] = None,
  tomatoesId: Option[String] = None,
  tomatoesRating: Option[String] = None,
  tmdbId: Option[String] = None,
  tmdbImg: Option[String] = None,
  tmdbRating: Option[String] = None
){

  def humanDate() = {
    format.format(datetime.atZone(Globals.timezone))
  }

  def imdbUrl = imdbId.map("http://www.imdb.com/title/%s".format(_))
  def tmdbUrl = tmdbId.map("http://www.themoviedb.org/movie/%s".format(_))
  def tomatoesUrl = tomatoesId.map("http://www.rottentomatoes.com/m/%s".format(_))

  override def toString = name + " " + channel + "@" + datetime
}


object Broadcast{

  private final val format = DateTimeFormatter.ofPattern("dd MMM HH:mm")

// TODO use this
  //implicit val broadcastFormat = Json.format[Broadcast]

  implicit object BroadcastBSONReader extends BSONDocumentReader[Broadcast] {
    def read(doc: BSONDocument) :Broadcast = {
      Broadcast(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[String]("name").get,
        doc.getAs[String]("channel").get,
        doc.getAs[BSONLong]("datetime").map(dt => Instant.ofEpochMilli(dt.value)).get,
        doc.getAs[BSONInteger]("year").map(y => y.value),
        doc.getAs[String]("humoId"),
        doc.getAs[String]("humoUrl"),
        doc.getAs[String]("yeloId"),
        doc.getAs[String]("yeloUrl"),
        doc.getAs[String]("belgacomId"),
        doc.getAs[String]("belgacomUrl"),
        doc.getAs[String]("imdbId"),
        doc.getAs[String]("tomatoesId"),
        doc.getAs[String]("tomatoesRating"),
        doc.getAs[String]("tmdbId"),
        doc.getAs[String]("tmdbImg"),
        doc.getAs[String]("tmdbRating")
      )
    }
  }

  implicit object BroadcastBSONWriter extends BSONDocumentWriter[Broadcast] {
    def write(broadcast: Broadcast) = {
      BSONDocument(
        "_id" -> broadcast.id.getOrElse(BSONObjectID.generate),
        "name" -> BSONString(broadcast.name),
        "channel" -> BSONString(broadcast.channel),
        //"datetime" -> movie.datetime.map(dt => BSONDateTime(dt.getMillis)),
        "datetime" -> BSONLong(broadcast.datetime.toEpochMilli),
        "year" -> broadcast.year.map(BSONInteger),
        "humoId" -> broadcast.humoId.map(BSONString),
        "humoUrl" -> broadcast.humoUrl.map(BSONString),
        "yeloId" -> broadcast.yeloId.map(BSONString),
        "yeloUrl" -> broadcast.yeloUrl.map(BSONString),
        "belgacomId" -> broadcast.belgacomId.map(BSONString),
        "belgacomUrl" -> broadcast.belgacomUrl.map(BSONString),
        "imdbId" -> broadcast.imdbId.map(BSONString),
        "tomatoesId" -> broadcast.tomatoesId.map(BSONString),
        "tomatoesRating" -> broadcast.tomatoesRating.map(BSONString),
        "tmdbId" -> broadcast.tmdbId.map(BSONString),
        "tmdbImg" -> broadcast.tmdbImg.map(BSONString),
        "tmdbRating" -> broadcast.tmdbRating.map(BSONString)
      )
    }
  }

}
