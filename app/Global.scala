import akka.actor.Props
import play.api.mvc.RequestHeader
import services.Mailer

import play.api.GlobalSettings
import play.api.Logger
import play.libs.Akka
import services.actors.Master
import controllers.Application

object Global extends GlobalSettings{

  override def onStart(app: play.api.Application){
    Logger.info("Scheduling actor trigger")
    Application.masterActorRef = Akka.system.actorOf(Props[Master], name = "masterActor")
    //Akka.system.scheduler.schedule(0 seconds, 12 hours, Application.masterActorRef, Start)
  }

  override def onError(request: RequestHeader, throwable: Throwable) = {
    if(play.Play.isProd){
      Logger.error("Server error: " + request.path, throwable)
      val message = s"Internal server error: ${throwable.getMessage}"
      Mailer.sendMail(message, throwable)
    }
    super.onError(request, throwable)
  }

}