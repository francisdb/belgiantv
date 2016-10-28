package helper

import akka.actor.Cancellable
import akka.stream.{ClosedShape, Graph, Materializer}
import org.specs2.execute.{AsResult, Result}
import org.specs2.mutable.Around
import org.specs2.specification.Scope
import play.api.Environment
import play.api.inject.DefaultApplicationLifecycle
import play.api.libs.ws.ahc.{AhcWSAPI, AhcWSClientConfig}

import scala.concurrent.duration.FiniteDuration

abstract class WithWS extends Around with Scope {
  val lifecycle = new DefaultApplicationLifecycle
  lazy val environment = Environment.simple()
  lazy val clientConfig = AhcWSClientConfig()
  implicit lazy val mat = NoMaterializer
  lazy val ws = new AhcWSAPI(environment, clientConfig, lifecycle)

  def around[T: AsResult](t: => T): Result = {
    try {
      AsResult.effectively(t)
    } finally {
      lifecycle.stop()
    }
  }
}


/**
  * copy from the play NoMaterializer
  */
private[helper] object NoMaterializer extends Materializer {
  def withNamePrefix(name: String) = throw new UnsupportedOperationException("NoMaterializer cannot be named")
  implicit def executionContext = throw new UnsupportedOperationException("NoMaterializer does not have an execution context")
  def materialize[Mat](runnable: Graph[ClosedShape, Mat]) =
    throw new UnsupportedOperationException("No materializer was provided, probably when attempting to extract a response body, but that body is a streamed body and so requires a materializer to extract it.")
  override def scheduleOnce(delay: FiniteDuration, task: Runnable): Cancellable =
    throw new UnsupportedOperationException("NoMaterializer can't schedule tasks")
  override def schedulePeriodically(initialDelay: FiniteDuration, interval: FiniteDuration, task: Runnable): Cancellable =
    throw new UnsupportedOperationException("NoMaterializer can't schedule tasks")
}