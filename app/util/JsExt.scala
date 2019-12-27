package util

import play.api.libs.json._

object JsExt {

  implicit class JsLookupResultExt(value: JsLookupResult) {
    def asOptSafe[T](implicit fjs: Reads[T]): Option[T] = value.toOption.flatMap {
      case n: JsNumber => fjs.reads(n).asOpt
      case s: JsString => None // do we want to try to parse the string?
      case o: JsValue  => None // fail here?
    }
  }

}
