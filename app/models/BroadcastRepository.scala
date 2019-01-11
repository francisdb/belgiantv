package models

import java.time.Instant

import models.Broadcast._
import org.threeten.extra.Interval
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class BroadcastRepository(val reactiveMongoApi: ReactiveMongoApi, executionContext: ExecutionContext) extends MongoSupport {
  // TODO inject MongoSupport to get rid of the mailer dependency?

  // TODO create specific executioncontext for mongo
  private implicit val ec: ExecutionContext = executionContext

  private lazy val dbFuture = reactiveMongoApi.connection.database(dbName, herokuMLabFailover)
  private lazy val broadcastCollectionFuture = dbFuture.map(_.collection[BSONCollection]("broadcasts"))

  //private lazy val broadcastCollectionFuture = Future.successful(reactiveMongoApi.db.collection[BSONCollection]("broadcasts"))

  protected override val logger = Logger("application.db")

  // TODO make all these properly anync

  def create(broadcast: Broadcast): Broadcast = {
    val id = BSONObjectID.generate
    val withId = broadcast.copy(id = Some(id))
    implicit val writer = Broadcast.BroadcastBSONWriter
    broadcastCollectionFuture.flatMap{ broadcastCollection =>
      broadcastCollection.insert(withId)(writer, scala.concurrent.ExecutionContext.Implicits.global)
    }.onComplete(le =>
      mongoLogger(le, "saved broadcast " + withId + " with id " + id))
    withId
  }

  def findByInterval(interval:Interval): Future[List[Broadcast]] = broadcastCollectionFuture.flatMap{ broadcastCollection =>

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

    broadcastCollection.find(BSONDocument(
        "datetime" -> BSONDocument(
          "$gt" -> BSONLong(interval.getStart.toEpochMilli)
        ),
        "datetime" -> BSONDocument(
          "$lt" ->  BSONLong(interval.getEnd.toEpochMilli)
        )
      ), Option.empty[Broadcast])
      .sort(BSONDocument(
        "datetime" -> BSONInteger(1))
      )
      .cursor[Broadcast]()
      .collect[List](1000, Cursor.FailOnError[List[Broadcast]]())

    //    broadcastCollectionJson.find(Json.obj(
    //        "datetime" -> Json.obj("$gt" -> interval.getStart.getMillis),
    //        "datetime" -> Json.obj("$lt" ->  interval.getEnd.getMillis)))
    //      .sort(Json.obj("datetime" -> 1)).cursor[Broadcast].toList
  }

  def findMissingTomatoes(): Future[List[Broadcast]] = broadcastCollectionFuture.flatMap{ broadcastCollection =>

    broadcastCollection.find(BSONDocument(
        "datetime" -> BSONDocument(
          "$gt" -> BSONLong(System.currentTimeMillis())
        ),
        "tomatoesId" -> BSONDocument(
          "$exists" -> BSONBoolean(value=false)
        )
      ), Option.empty[Broadcast])
      .sort(BSONDocument("datetime" -> BSONInteger(1)))
      .cursor[Broadcast]()
      .collect[List](1000, Cursor.FailOnError[List[Broadcast]]())


    //    broadcastCollectionJson
    //      .find(Json.obj(
    //        "datetime" -> Json.obj("$gt" -> System.currentTimeMillis()),
    //        "tomatoesId" -> Json.obj("$exists" -> false)))
    //      .sort(Json.obj("datetime" -> 1)).cursor[Broadcast].toList
  }

  def findByDateTimeAndChannel(datetime: Instant, channel: String): Future[Option[Broadcast]] = broadcastCollectionFuture.flatMap{ broadcastCollection =>
    //    broadcastCollectionJson.find(Json.obj(
    //      "datetime" -> datetime.getMillis,
    //      "channel" -> channel
    //    )).cursor[Broadcast].headOption

    broadcastCollection.find(BSONDocument(
        "datetime" -> BSONLong(datetime.toEpochMilli),
        "channel" -> BSONString(channel)), Option.empty[Broadcast])
      .cursor[Broadcast]()
      .headOption
  }

  def setYelo(b:Broadcast, yeloId:String, yeloUrl:Option[String]): Unit = {
    broadcastCollectionFuture.flatMap{ broadcastCollection =>
      broadcastCollection.update(
        BSONDocument("_id" -> b.id),
        BSONDocument(
          "$set" -> BSONDocument(
            "yeloId" -> yeloId,
            "yeloUrl" -> yeloUrl
          )
        )
      )
    }.onComplete(le => mongoLogger(le, s"updated yelo for $b"))
  }

  def setBelgacom(b:Broadcast, belgacomId:String, belgacomUrl:String): Unit = {
    broadcastCollectionFuture.flatMap { broadcastCollection =>
      broadcastCollection.update(
        BSONDocument("_id" -> b.id),
        BSONDocument(
          "$set" -> BSONDocument(
            "belgacomId" -> belgacomId,
            "belgacomUrl" -> belgacomUrl
          )
        )
      )
    }.onComplete(le => mongoLogger(le, s"updated belgacom for $b"))
  }

  def setImdb(b:Broadcast, imdbId:String): Unit = {
    broadcastCollectionFuture.flatMap { broadcastCollection =>
      broadcastCollection.update(
        BSONDocument("_id" -> b.id),
        BSONDocument(
          "$set" -> BSONDocument(
            "imdbId" -> BSONString(imdbId)
          )
        )
      )
    }.onComplete(le => mongoLogger(le, s"updated imdb for $b"))
  }

  def setTomatoes(broadcastId:BSONObjectID, tomatoesId:String, tomatoesRating:Option[String]): Unit = {
    broadcastCollectionFuture.flatMap { broadcastCollection =>
      broadcastCollection.update(
        BSONDocument("_id" -> broadcastId),
        BSONDocument(
          "$set" -> BSONDocument(
            "tomatoesId" -> tomatoesId,
            "tomatoesRating" -> tomatoesRating
          )
        )
      )
    }.onComplete(le => mongoLogger(le, s"updated tomatoes for $broadcastId"))
  }

  def setTmdb(b:Broadcast, tmdbId:String, tmdbRating: Option[String]): Unit = {
    broadcastCollectionFuture.flatMap { broadcastCollection =>
      broadcastCollection.update(
        BSONDocument("_id" -> b.id),
        BSONDocument(
          "$set" -> BSONDocument(
            "tmdbId" -> tmdbId,
            "tmdbRating" -> tmdbRating
          )
        )
      )
    }.onComplete(le => mongoLogger(le, s"updated tmdb for $b"))
  }

  def setTmdbImg(b:Broadcast, tmdbImg:String): Unit = {
    broadcastCollectionFuture.flatMap { broadcastCollection =>
      broadcastCollection.update(
        BSONDocument("_id" -> b.id),
        BSONDocument(
          "$set" -> BSONDocument(
            "tmdbImg" -> tmdbImg
          )
        )
      )
    }.onComplete(le => mongoLogger(le, s"updated tmdb img for $b"))
  }
}
