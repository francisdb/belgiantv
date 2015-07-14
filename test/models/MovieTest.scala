package models

import _root_.helper.ConfigSpec
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import play.api.test.Helpers._
import play.api.test.FakeApplication

import scala.concurrent._
import scala.concurrent.duration._

class MovieTest extends Specification with NoTimeConversions with ConfigSpec {

  private val configProperty = "mongodb.uri"

  "trying to read a movie" should {
    "return data" in {

      // TODO use https://github.com/athieriot/specs2-embedmongo

      val app = FakeApplication()
      running(app) {
        skipIfMissingConfig(configProperty)
        val movieRepository = app.injector.instanceOf[MovieRepository]
        val result = Await.result(movieRepository.find("ddd", Some(1980)), 30.seconds)
        //println(result.isDefined)
        result.isDefined must beFalse
      }

    }
  }
}
