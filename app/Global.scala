import akka.actor.ActorRef
import akka.actor.Props
import akka.util.duration.intToDurationInt
import play.api.GlobalSettings
import play.api.Logger
import play.libs.Akka
import scala.annotation.implicitNotFound
import services.akka.Master
import services.akka.Start
import controllers.Application

object Global extends GlobalSettings{


  
  override def onStart(app: play.api.Application){
    Logger.info("Scheduling actor trigger")
    Application.masterActorRef = Akka.system.actorOf(Props[Master], name = "masterActor")
    //Akka.system.scheduler.schedule(0 seconds, 12 hours, Application.masterActorRef, Start)
  }

}