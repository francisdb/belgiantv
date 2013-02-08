package models

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import play.api.test.Helpers._
import play.api.test.FakeApplication

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

class MovieTest extends Specification with NoTimeConversions {

  "trying to read a movie" should {
    "return data" in {
      running(FakeApplication()) {
        val result = Await.result(Movie.find("ddd", Some(1980)), 30 seconds)
        println(result.isDefined)
      }
    }
  }
}
