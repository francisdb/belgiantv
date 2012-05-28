package services.akka

import play.api.Logger
import akka.actor.{Props, Actor}
import akka.routing.RoundRobinRouter
import collection.mutable.ListBuffer
import services.HumoReader
import services.ImdbApiService
import controllers.Application
import services.TomatoesApiService
import models.Movie

class BelgianTvActor extends Actor {
  
//  val numOfTweets = 10
//  val workerRouter = context.actorOf(Props[WebScrapperWorker]
//                                     .withRouter(RoundRobinRouter(numOfTweets)),
//                                     name = "twitterSearch")

  //var sendMessages = 0;
  //var receivedMessages = 0;
  //var list: ListBuffer[TweetNews] = new ListBuffer[TweetNews]

//  override def preStart() {
//	  // registering with other actors
//	  //someService ! Register(self)
//    Logger.debug("[" + this + "] - preStart()")
//  }
  
  protected def receive: Receive = {

    case msg: SearchImdb => {
      Logger.info("[" + this + "] - Received [" + msg + "] from " + sender)
      //Logger.warn("not implemented")
      val movie = ImdbApiService.findOrRead(msg.title, msg.year)
      println("IMDB result -> " + movie)
      movie.map{ m =>
        val dbMovie = new Movie(null, m.title, m.id, m.rating)
        Movie.create(dbMovie)
      }
      
    }
    
    case msg: SearchTomatoes => {
      Logger.info("[" + this + "] - Received [" + msg + "] from " + sender)
      val movie = TomatoesApiService.find(msg.title, msg.year)
      println("Tomatoes result -> " + movie)
    }

    case msg: FetchHumo => {
      Logger.info("[" + this + "] - Received [" + msg + "] from " + sender)
      
      HumoReader.fetchDay(msg.day, Application.channelFilter).onRedeem{ movies =>
        Logger.info("[" + this + "] - onRedeem " + movies.size + " movies")
        movies.foreach{ event =>
          self ! SearchImdb(event.title, event.year)
          self ! SearchTomatoes(event.title, event.year)
        }
      }
      
    }
    
    case _ => {
      Logger.warn("[" + this + "] - Received unknown message from " + sender)
    }
  }
}