package helper

import akka.stream._
import org.specs2.specification.AfterAll
import play.api.libs.ws.ahc.{AhcWSClient, StandaloneAhcWSClient}
import play.shaded.ahc.org.asynchttpclient.{DefaultAsyncHttpClient, DefaultAsyncHttpClientConfig}

/**
  * Use this instead of AfterAll and call super.afterAll when done
  *
  * Without this construct these are not composeable
  * TODO find out if there is a cleaner solution
  */
trait BaseAfterAll extends AfterAll{
  override def afterAll = ()
}

trait WithMaterializer extends BaseAfterAll{
  //implicit val sys = ActorSystem("test")
  private val actorMaterializer = play.api.test.NoMaterializer// ActorMaterializer()
  implicit val mat: Materializer = actorMaterializer

  override def afterAll = {
    //actorMaterializer.shutdown()
    //Await.result(sys.terminate(), 10.seconds)
    super.afterAll
  }
}

trait WithWsClient extends WithMaterializer{
  private val clientConfig = new DefaultAsyncHttpClientConfig.Builder().build()
  val client = new DefaultAsyncHttpClient(clientConfig)
  val standaloneAhcWSClient = new StandaloneAhcWSClient(client)
  val ws = new AhcWSClient(standaloneAhcWSClient)

  override def afterAll = {
    ws.close()
    super.afterAll
  }
}


