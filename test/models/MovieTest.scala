package models

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import play.api.test.Helpers._
import play.api.test.FakeApplication

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import controllers.Application

import reactivemongo.api._
import reactivemongo.core.commands._
import services.PlayUtil

class MovieTest extends Specification with NoTimeConversions {

  skipAllUnless(PlayUtil.configExists("mongodb.uri"))


  "trying to read a movie" should {
    "return data" in {

      // TODO try to get the driver working without the fake app
//      lazy val driver = new MongoDriver
//      lazy val connection = driver.connection(servers, auth)
//      lazy val db = DB(dbName, connection)

      //val fongo = new Fongo("test mongo")

      running(FakeApplication()) {
        //Application.db = fongo.getDB("testdb")
        val result = Await.result(Movie.find("ddd", Some(1980)), 30 seconds)
        println(result.isDefined)
      }

    }
  }
}
