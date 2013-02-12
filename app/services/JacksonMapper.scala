package services

import java.lang.reflect.{Type, ParameterizedType}

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.core.JsonParseException
import play.api.Logger

trait JacksonMapper {
  private val logger = Logger("application.imdb")

  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  protected def deserialize[T: Manifest](value: String) : T = {
    try{
      mapper.readValue(value, new TypeReference[T]() {
        override def getType = new ParameterizedType {
          val getActualTypeArguments = manifest[T].typeArguments.map(_.runtimeClass.asInstanceOf[Type]).toArray
          val getRawType = manifest[T].runtimeClass
          val getOwnerType = null
        }
      })
    }catch{
      case ex:JsonParseException =>
        Logger.error(value)
        throw ex;
    }
  }
}
