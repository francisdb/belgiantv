package models

import _root_.helper.ConfigSpec
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification

import scala.concurrent._
import scala.concurrent.duration._

class MovieTest(implicit env: ExecutionEnv) extends Specification with ConfigSpec {

  "trying to read a movie" should {

    "return data" in {

      // TODO use https://github.com/athieriot/specs2-embedmongo

      skipped("need to implement this using specs2-embedmongo")
      val reactiveMongoApi = ???
      val movieRepository = new MovieRepository(reactiveMongoApi, env.executionContext)
      val result = Await.result(movieRepository.find("ddd", Some(1980)), 30.seconds)
      //println(result.isDefined)
      result.isDefined must beFalse
    }
  }
}
