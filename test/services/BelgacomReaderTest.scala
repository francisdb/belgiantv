package services


import java.time.LocalDate

import helper.WithWsClient
import org.specs2.mutable._
import services.proximus.BelgacomReader

import scala.concurrent._
import scala.concurrent.duration._

class BelgacomReaderTest extends Specification with WithWsClient {

 "the movie search for today" should {

    "return data" in {
      val belgacom = new BelgacomReader(ws)
      val today = LocalDate.now(BelgacomReader.timeZone)
      val result = Await.result(belgacom.searchMovies(today), 60.seconds)
      //println(result.map(_.channelName).toSet)
      //	      result.map{ movie =>
      //	          println(movie)
      //	      }
      val movie = result.head
      movie.program.getStart.atZone(BelgacomReader.timeZone).toLocalDate must be equalTo today
      movie.program.detailUrl must not contain "null"
      movie.program.title must not beEmpty
      //result must not be empty
    }
  }

  "the movie search for tomorrow" should {

    "return data" in {
      val belgacom = new BelgacomReader(ws)
      val tomorrow = LocalDate.now(BelgacomReader.timeZone).plusDays(1)
      val result = Await.result(belgacom.searchMovies(tomorrow), 60.seconds)
      //println(result.map(_.channelName).toSet)
      //	      result.map{ movie =>
      //	          println(movie)
      //	      }
      val movie = result.head
      movie.program.getStart.atZone(BelgacomReader.timeZone).toLocalDate must be equalTo tomorrow
      movie.program.detailUrl must not contain "null"
      movie.program.title must not beEmpty
      //result must not be empty

    }
  }
}
