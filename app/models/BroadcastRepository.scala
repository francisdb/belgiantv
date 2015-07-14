package models

import javax.inject.Inject

import models.Broadcast._
import org.joda.time.{DateTime, Interval}
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._
import services.Mailer

import scala.concurrent.Future

class BroadcastRepository @Inject() (val mailer: Mailer, val reactiveMongoApi: ReactiveMongoApi) extends MongoSupport {
  // TODO inject MongoSupport to get rid of the mailer dependency?

  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  lazy val db = reactiveMongoApi.db

  protected override val logger = Logger("application.db")

  private lazy val broadcastCollection = reactiveMongoApi.db.collection[BSONCollection]("broadcasts")
  private lazy val broadcastCollectionJson = reactiveMongoApi.db.collection[JSONCollection]("broadcasts")


  // TODO make all these properly anync

  def create(broadcast: Broadcast) = {
    val id = BSONObjectID.generate
    val withId = broadcast.copy(id = Some(id))
    implicit val writer = Broadcast.BroadcastBSONWriter
    broadcastCollection.insert(withId)(writer, scala.concurrent.ExecutionContext.Implicits.global)
      .onComplete(le => mongoLogger(le, "saved broadcast " + withId + " with id " + id))
    withId
  }

  def findByInterval(interval:Interval): Future[List[Broadcast]] = {

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

    broadcastCollection
      .find(BSONDocument(
        "datetime" -> BSONDocument("$gt" -> BSONLong(interval.getStart.getMillis)),
        "datetime" -> BSONDocument("$lt" ->  BSONLong(interval.getEnd.getMillis))))
      .sort(BSONDocument("datetime" -> BSONInteger(1)))
      .cursor[Broadcast]()
      .collect[List]()

    //    broadcastCollectionJson.find(Json.obj(
    //        "datetime" -> Json.obj("$gt" -> interval.getStart.getMillis),
    //        "datetime" -> Json.obj("$lt" ->  interval.getEnd.getMillis)))
    //      .sort(Json.obj("datetime" -> 1)).cursor[Broadcast].toList
  }

  def findMissingTomatoes(): Future[List[Broadcast]] = {

    broadcastCollection
      .find(BSONDocument(
        "datetime" -> BSONDocument("$gt" -> BSONLong(System.currentTimeMillis())),
        "tomatoesId" -> BSONDocument("$exists" -> BSONBoolean(value=false))))
      .sort(BSONDocument("datetime" -> BSONInteger(1)))
      .cursor[Broadcast]()
      .collect[List]()

    //    broadcastCollectionJson
    //      .find(Json.obj(
    //        "datetime" -> Json.obj("$gt" -> System.currentTimeMillis()),
    //        "tomatoesId" -> Json.obj("$exists" -> false)))
    //      .sort(Json.obj("datetime" -> 1)).cursor[Broadcast].toList
  }

  def findByDateTimeAndChannel(datetime: DateTime, channel: String): Future[Option[Broadcast]] = {
    //    broadcastCollectionJson.find(Json.obj(
    //      "datetime" -> datetime.getMillis,
    //      "channel" -> channel
    //    )).cursor[Broadcast].headOption

    broadcastCollection
      .find(BSONDocument(
        "datetime" -> BSONLong(datetime.getMillis),
        "channel" -> BSONString(channel)))
      .cursor[Broadcast]()
      .headOption
  }

  def setYelo(b:Broadcast, yeloId:String, yeloUrl:Option[String]) {
    broadcastCollection.update(
      BSONDocument("_id" -> b.id),
      BSONDocument("$set" -> BSONDocument("yeloId" -> yeloId, "yeloUrl" -> yeloUrl)))
      .onComplete(le => mongoLogger(le, "updated yelo for " + b))
  }

  def setBelgacom(b:Broadcast, belgacomId:String, belgacomUrl:String) {
    broadcastCollection.update(
      BSONDocument("_id" -> b.id),
      BSONDocument("$set" -> BSONDocument("belgacomId" -> belgacomId, "belgacomUrl" -> belgacomUrl)))
      .onComplete(le => mongoLogger(le, "updated belgacom for " + b))
  }

  def setImdb(b:Broadcast, imdbId:String) {
    broadcastCollection.update(
      BSONDocument("_id" -> b.id),
      BSONDocument("$set" -> BSONDocument("imdbId" -> BSONString(imdbId))))
      .onComplete(le => mongoLogger(le, "updated imdb for " + b))
  }

  def setTomatoes(broadcastId:BSONObjectID, tomatoesId:String, tomatoesRating:Option[String]) {
    broadcastCollection.update(
      BSONDocument("_id" -> broadcastId),
      BSONDocument("$set" -> BSONDocument(
        "tomatoesId" -> tomatoesId,
        "tomatoesRating" -> tomatoesRating)))
      .onComplete(le => mongoLogger(le, "updated tomatoes for " + broadcastId))
  }

  def setTmdb(b:Broadcast, tmdbId:String, tmdbRating:String) {
    broadcastCollection.update(
      BSONDocument("_id" -> b.id),
      BSONDocument("$set" -> BSONDocument(
        "tmdbId" -> tmdbId,
        "tmdbRating" -> tmdbRating)))
      .onComplete(le => mongoLogger(le, "updated tmdb for " + b))
  }

  def setTmdbImg(b:Broadcast, tmdbImg:String) {
    broadcastCollection.update(
      BSONDocument("_id" -> b.id),
      BSONDocument("$set" -> BSONDocument("tmdbImg" -> tmdbImg)))
      .onComplete(le => mongoLogger(le, "updated tmdb img for " + b))
  }
}