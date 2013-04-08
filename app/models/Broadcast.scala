package models

import play.api.Logger

import org.joda.time.{Interval, DateTime}
import org.joda.time.format.DateTimeFormat
import controllers.Application
import concurrent.Future

import reactivemongo.bson._
import reactivemongo.bson.handlers.{BSONWriter, BSONReader}
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler

import reactivemongo.api.QueryBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import util.{Try, Failure, Success}
import reactivemongo.core.commands.LastError
import reactivemongo.bson.BSONString
import scala.Some
import reactivemongo.api.QueryBuilder
import reactivemongo.bson.BSONLong
import reactivemongo.bson.BSONInteger

case class Broadcast(
  id: Option[BSONObjectID],
  name: String,
  channel: String,
// TODO remove option for datetime
  datetime: DateTime,
  year: Option[Int] = None,
  humoId: Option[String] = None,
  humoUrl: Option[String] = None,
  yeloId: Option[String] = None,
  yeloUrl: Option[String] = None,
  belgacomId: Option[String] = None,
  belgacomUrl: Option[String] = None,
  imdbId: Option[String] = None,
  tomatoesId: Option[String] = None,
  tmdbId: Option[String] = None,
  tmdbImg: Option[String] = None
){

  def humanDate() = {
    val format = DateTimeFormat.forPattern("dd MMM HH:mm")
    format.print(datetime.withZone(Application.timezone))
  }

  def imdbUrl = imdbId.map("http://www.imdb.com/title/%s".format(_))
  def tmdbUrl = tmdbId.map("http://www.themoviedb.org/movie/%s".format(_))
  def tomatoesUrl = tomatoesId.map("http://www.rottentomatoes.com/m/%s".format(_))

  override def toString() = name + " " + channel + "@" + datetime
}


object Broadcast extends MongoSupport{
  
  protected override val logger = Logger("application.db")

  private lazy val broadcastCollection = Application.db("broadcasts")

  implicit object BroadcastBSONReader extends BSONReader[Broadcast] {
    def fromBSON(document: BSONDocument) :Broadcast = {
      val doc = document.toTraversable
      Broadcast(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[BSONString]("name").get.value,
        doc.getAs[BSONString]("channel").get.value,
        doc.getAs[BSONLong]("datetime").map(dt => new DateTime(dt.value)).get,
        doc.getAs[BSONInteger]("year").map(y => y.value),
        doc.getAs[BSONString]("humoId").map(i => i.value),
        doc.getAs[BSONString]("humoUrl").map(i => i.value),
        doc.getAs[BSONString]("yeloId").map(i => i.value),
        doc.getAs[BSONString]("yeloUrl").map(i => i.value),
        doc.getAs[BSONString]("belgacomId").map(i => i.value),
        doc.getAs[BSONString]("belgacomUrl").map(i => i.value),
        doc.getAs[BSONString]("imdbId").map(i => i.value),
        doc.getAs[BSONString]("tomatoesId").map(i => i.value),
        doc.getAs[BSONString]("tmdbId").map(i => i.value),
        doc.getAs[BSONString]("tmdbImg").map(i => i.value)
      )
    }
  }

  implicit object BroadcastBSONWriter extends BSONWriter[Broadcast] {
    def toBSON(broadcast: Broadcast) = {
      BSONDocument(
        "_id" -> broadcast.id.getOrElse(BSONObjectID.generate),
        "name" -> BSONString(broadcast.name),
        "channel" -> BSONString(broadcast.channel),
        //"datetime" -> movie.datetime.map(dt => BSONDateTime(dt.getMillis)),
        "datetime" -> BSONLong(broadcast.datetime.getMillis),
        "year" -> broadcast.year.map(BSONInteger(_)),
        "humoId" -> broadcast.humoId.map(BSONString(_)),
        "humoUrl" -> broadcast.humoUrl.map(BSONString(_)),
        "yeloId" -> broadcast.yeloId.map(BSONString(_)),
        "yeloUrl" -> broadcast.yeloUrl.map(BSONString(_)),
        "belgacomId" -> broadcast.belgacomId.map(BSONString(_)),
        "belgacomUrl" -> broadcast.belgacomUrl.map(BSONString(_)),
        "imdbId" -> broadcast.imdbId.map(BSONString(_)),
        "tomatoesId" -> broadcast.tomatoesId.map(BSONString(_)),
        "tmdbId" -> broadcast.tmdbId.map(BSONString(_)),
        "tmdbImg" -> broadcast.tmdbImg.map(BSONString(_))
      )
    }
  }


  // TODO make all these properly anync

  def create(broadcast: Broadcast) = {
    val id = BSONObjectID.generate
    val withId = broadcast.copy(id = Some(id))
    broadcastCollection.insert(withId)
      .onComplete(le => mongoLogger(le, "saved broadcast " + withId + " with id " + id))
    withId
  }

  def findByInterval(interval:Interval): Future[List[Broadcast]] = {

    val broadcastsInInterval = QueryBuilder()
      .query(BSONDocument(
      "datetime" -> BSONDocument("$gt" -> BSONLong(interval.getStart.getMillis)),
      "datetime" -> BSONDocument("$lt" ->  BSONLong(interval.getEnd.getMillis))))
      .sort(BSONDocument("datetime" -> BSONInteger(1)))

    //    val broadcastsInInterval = BSONDocument(
    //      "$orderby" -> BSONDocument("datetime" -> BSONInteger(1)),
    //      "$query" -> BSONDocument(
    //        "datetime" -> BSONDocument("$gt" -> BSONLong(interval.getStart.getMillis)),
    //        "datetime" -> BSONDocument("$lt" ->  BSONLong(interval.getEnd.getMillis))
    //      )
    //    )

    //    val broadcastsInInterval = QueryBuilder().query( Json.obj(
    //            "datetime" -> Json.obj("$gt" -> interval.getStart.getMillis),
    //            "datetime" -> Json.obj("$lt" -> interval.getEnd.getMillis)))//.sort( "created" -> SortOrder.Descending)

    broadcastCollection.find(broadcastsInInterval).toList
  }

  def findMissingTomatoes(): Future[List[Broadcast]] = {
    val broadcastsInInterval = QueryBuilder()
    .query(BSONDocument(
      "datetime" -> BSONDocument("$gt" -> BSONLong(System.currentTimeMillis())),
      "tomatoesId" -> BSONDocument("$exists" -> BSONBoolean(false)))
    )
    .sort(BSONDocument("datetime" -> BSONInteger(1)))

    broadcastCollection.find(broadcastsInInterval).toList
  }

  def findByDateTimeAndChannel(datetime: DateTime, channel: String): Future[Option[Broadcast]] = {
    broadcastCollection.find(BSONDocument(
      "datetime" -> BSONLong(datetime.getMillis),
      "channel" -> BSONString(channel)
    )).headOption()
  }

  def setYelo(b:Broadcast, yeloId:String, yeloUrl:String) {
    broadcastCollection.update(
      BSONDocument("_id" -> b.id),
      BSONDocument("$set" -> BSONDocument("yeloId" -> BSONString(yeloId), "yeloUrl" -> BSONString(yeloUrl))))
      .onComplete(le => mongoLogger(le, "updated yelo for " + b))
  }

  def setBelgacom(b:Broadcast, belgacomId:String, belgacomUrl:String) {
    broadcastCollection.update(
      BSONDocument("_id" -> b.id),
      BSONDocument("$set" -> BSONDocument("belgacomId" -> BSONString(belgacomId), "belgacomUrl" -> BSONString(belgacomUrl))))
      .onComplete(le => mongoLogger(le, "updated belgacom for " + b))
  }

  def setImdb(b:Broadcast, imdbId:String) {
    broadcastCollection.update(
      BSONDocument("_id" -> b.id),
      BSONDocument("$set" -> BSONDocument("imdbId" -> BSONString(imdbId))))
      .onComplete(le => mongoLogger(le, "updated imdb for " + b))
  }

  def setTomatoes(broadcastId:BSONObjectID, tomatoesId:String) {
    broadcastCollection.update(
      BSONDocument("_id" -> broadcastId),
      BSONDocument("$set" -> BSONDocument("tomatoesId" -> BSONString(tomatoesId))))
      .onComplete(le => mongoLogger(le, "updated tomatoes for " + broadcastId))
  }

  def setTmdb(b:Broadcast, tmdbId:String) {
    broadcastCollection.update(
      BSONDocument("_id" -> b.id),
      BSONDocument("$set" -> BSONDocument("tmdbId" -> BSONString(tmdbId))))
      .onComplete(le => mongoLogger(le, "updated tmdb for " + b))
  }

  def setTmdbImg(b:Broadcast, tmdbImg:String) {
    broadcastCollection.update(
      BSONDocument("_id" -> b.id),
      BSONDocument("$set" -> BSONDocument("tmdbImg" -> BSONString(tmdbImg))))
      .onComplete(le => mongoLogger(le, "updated tmdb img for " + b))
  }

}