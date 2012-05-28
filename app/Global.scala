import play.api.GlobalSettings
import play.api.Application

import play.libs.Akka

import akka.actor.Props
import akka.util.duration._

import services.akka.{Start, Master}

object Global extends GlobalSettings{

  override def onStart(app: Application){
    //Akka.system.scheduler.schedule(0 seconds, 10 minutes, masterActorRef, Start)
    
    val masterActorRef = Akka.system.actorOf(Props[Master], name = "masterActor")
    
    masterActorRef ! Start
  }
}