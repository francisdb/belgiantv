package services
import play.api.libs.ws.WS
import play.api.Logger
import models.helper.ImdbApiMovie
import org.codehaus.jackson.map.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.codehaus.jackson.`type`.TypeReference
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object ImdbApiService {

  private val logger = Logger("application.imdb")
  
  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def findOrRead(title: String, year: Option[Int] = None) = {
    val url = "http://www.imdbapi.com/"
    var request = WS.url(url).withQueryString("t" -> title)
    if (year.isDefined) {
      request = request.withQueryString("y" -> year.get.toString())
    }
    
    logger.info("Fetching " + request)
    
    request.get.map{ response =>
	      val json = response.json
	      val error = (json \ "Error").asOpt[String]
	      if(error.isDefined){
	        val msg = "IMDB api internal error for %s %s: %s".format(title, year, error.get)
	        logger.warn(msg)
	        println(msg)
	        None
	      }else{
	        val movie = deserialize[ImdbApiMovie](response.body)
	        Option(movie)
	      }
    }
  }
  
  private def deserialize[T: Manifest](value: String) : T =
    mapper.readValue(value, new TypeReference[T]() {
      override def getType = new ParameterizedType {
        val getActualTypeArguments = manifest[T].typeArguments.map(_.erasure.asInstanceOf[Type]).toArray
        val getRawType = manifest[T].erasure
        val getOwnerType = null
      }
  })
}