import com.typesafe.config.ConfigFactory
import models.Broadcast
import org.specs2.mutable.Specification
import reactivemongo.api.{Cursor, FailoverStrategy, MongoConnection, MongoDriver}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ReactiveMongoTest extends Specification{

  "reactivemongo" should {

    "connect to mongolab" in {

      skipped("was for https://groups.google.com/forum/#!topic/reactivemongo/x3OZ6-hIIXk")

      val config = ConfigFactory.load()
      val driver = new MongoDriver()
      val uri = MongoConnection.parseURI(config.getString("mongodb.uri")).get
      val connection = driver.connection(uri)

      val dbFuture = connection.database(uri.db.get, FailoverStrategy(100.milliseconds, 20, {n => val w = n * 2; println(w); w}))
      //val dbFuture = connection.database(uri.db.get)
      val q = dbFuture.flatMap { db =>
        val broadcastCollection = db[BSONCollection]("broadcasts")
        broadcastCollection
          .find(BSONDocument(), None)
          .cursor[Broadcast]()
          .collect[List](1, Cursor.FailOnError[List[Broadcast]]())
      }

      val result = Await.result(q, 20.seconds)

      //result.foreach(println)

      connection.askClose()(10.seconds)
      driver.close()

      true must beTrue
    }

  }
}
