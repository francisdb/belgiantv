package util

import play.api.libs.json.{JsString, JsNumber, Reads, JsValue}

object JsExt {

  implicit class JsValueExt(value: JsValue) {
    def asOptSafe[T](implicit fjs: Reads[T]): Option[T] = value match {
      case n: JsNumber => fjs.reads(value).asOpt
      case s: JsString => None // do we want to try to parse the string?
    }
  }

}