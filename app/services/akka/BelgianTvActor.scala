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
import models.Broadcast
import services.YeloReader
import java.util.TimeZone
import org.joda.time.DateTimeZone
import services.TmdbApiService

class BelgianTvActor extends Actor {
  
  val logger = Logger("application.actor")
  
//  val numOfTweets = 10
//  val workerRouter = context.actorOf(Props[WebScrapperWorker]
//                                     .withRouter(RoundRobinRouter(numOfTweets)),
//                                     name = "twitterSearch")

  //var sendMessages = 0;
  //var receivedMessages = 0;
  //var list: ListBuffer[TweetNews] = new ListBuffer[TweetNews]
  
  override def preRestart(reason: Throwable, message: Option[Any]) {
    logger.error("Restarting due to [{}] when processing [{}]".format(reason.getMessage, message.getOrElse("")), reason)
  }
  
  
  protected def receive: Receive = {

    case msg: LinkTmdb => {
      logger.info("[" + this + "] - Received [" + msg + "] from " + sender)
      
      val service = new TmdbApiService()
      
      val tmdbmovie = msg.broadcast.year.map{ year =>
        service.findOrRead(msg.broadcast.name, msg.broadcast.year.get)
      }.getOrElse(service.findOrRead(msg.broadcast.name))
      
      if(tmdbmovie != null){
        Broadcast.setTmdb(msg.broadcast, tmdbmovie.id.toString)
        val poster = Option.apply(tmdbmovie.getFirstPoster())
        poster.map(Broadcast.setTmdbImg(msg.broadcast, _))
      }else{
        logger.warn("No TMDb movie found for %s (%s)".format(msg.broadcast.name, msg.broadcast.year))
      }
    }
    
    case msg: LinkImdb => {
      logger.info("[" + this + "] - Received [" + msg + "] from " + sender)
      
      val movie = msg.broadcast.year.map(year => 
        Movie.findByNameAndYear(msg.broadcast.name, year)
      ).getOrElse(Movie.findByName(msg.broadcast.name))
      
      
      val movie2 = movie.orElse {
        val movie = ImdbApiService.findOrRead(msg.broadcast.name, msg.broadcast.year)
	    
	    movie.map{ m =>
	      val dbMovie = new Movie(null, m.title, m.id, m.rating, m.year, m.poster)
	      val created = Movie.create(dbMovie)
	      created
	    }
      }
      
      movie2.map{ m =>
      	Broadcast.setImdb(msg.broadcast, m.getImdbId)
      }.getOrElse(
          logger.warn("No IMDB movie found for %s (%s)".format(msg.broadcast.name, msg.broadcast.year))
      )
      
    }
    
    case msg: LinkTomatoes => {
      logger.info("[" + this + "] - Received [" + msg + "] from " + sender)
      val movie = TomatoesApiService.find(msg.broadcast.name, msg.broadcast.year)
      logger.info("Tomatoes result -> " + movie)
    }

    case msg: FetchHumo => {
      logger.info("[" + this + "] - Received [" + msg + "] from " + sender)
      
      HumoReader.fetchDay(msg.day, Application.channelFilter).onRedeem{ movies =>
        
        val broadcasts = movies.map{ event =>
          val broadcast = new Broadcast(null, event.title, event.channel.toLowerCase, event.toDateTime, event.year, 
              humoId = Option.apply(event.id), humoUrl = Option.apply(event.url))
          val fromDB = Broadcast.findByDateTimeAndChannel(broadcast.datetime, broadcast.channel)
          fromDB.map{ broadcast =>
        	if(broadcast.imdbId.isEmpty){
        	  self ! LinkImdb(broadcast)
        	}
        	broadcast
          }.getOrElse{
            val saved = Broadcast.create(broadcast)
        	self ! LinkImdb(saved)
        	self ! LinkTmdb(saved)
        	//self ! LinkTomatoes(broadcast)
        	saved
          }
        }
        
        self ! FetchYelo(msg.day, broadcasts)
      }
    }
    
    case msg: FetchYelo => {
      logger.info("[" + this + "] - Received [" + msg + "] from " + sender)
      
      YeloReader.fetchDay(msg.day, Application.channelFilter).onRedeem{ movies =>
        
        // all unique channels
        // println(movies.groupBy{_.channel}.map{_._1})
        
        msg.events.foreach{ broadcast =>
          if(broadcast.yeloUrl.isEmpty){
	          val found = movies.filter(yeloMovie => YeloReader.yeloChannelToHumoChannel(yeloMovie.channel).toLowerCase == broadcast.channel.toLowerCase && yeloMovie.toDateTime.withZone(DateTimeZone.UTC) == broadcast.datetime.withZone(DateTimeZone.UTC)).headOption
	          found.map{ event =>
	            Broadcast.setYelo(broadcast, event.id.toString, event.url)
	          }.getOrElse{
	            logger.warn("No yelo match for " + broadcast.channel + " " + broadcast.humanDate + " " + broadcast.name)
	          }
          }
        }
      }   
    }
    
    case x => {
      logger.warn("[" + this + "] - Received unknown message [" + x + "] from " + sender)
    }
  }
}



