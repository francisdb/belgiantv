package services.tomatoes

import play.api.libs.json._

private[tomatoes] object TomatoesReaders{
  implicit val idsReads = Json.reads[TomatoesAlternateIds]
  implicit val ratingsReads = Json.reads[TomatoesRatings]
  //implicit val movieReads = Json.reads[TomatoesMovie]
  implicit val movieReads = new Reads[TomatoesMovie] {
    override def reads(json: JsValue): JsResult[TomatoesMovie] = {
      import _root_.util.JsExt._
      val id = (json \ "id").as[String]
      val title = (json \ "title").as[String]
      val year = (json \ "year").asOptSafe[Int]
      val runtime = (json \ "runtime").asOptSafe[Int]
      val ratings = (json \ "ratings").as[TomatoesRatings]
      val alternate_ids = (json \ "alternate_ids").asOpt[TomatoesAlternateIds]
      val critics_consensus = (json \ "critics_consensus").asOpt[String]
      JsSuccess(TomatoesMovie(id, title, year, runtime, ratings, alternate_ids, critics_consensus))
    }
  }
  implicit val searchReads = Json.reads[TomatoesSearch]
}

case class TomatoesSearch(
    total:Int, 
    movies:List[TomatoesMovie]) {	
}

case class TomatoesMovie(
    id:String,
    title:String,
    // annoying, this field can be 1999 or ""
    year:Option[Int],
    runtime:Option[Int],
    ratings:TomatoesRatings, 
    alternate_ids:Option[TomatoesAlternateIds],
    critics_consensus:Option[String]) {
}

case class TomatoesRatings (
    critics_rating:Option[String], 
    critics_score:Int, 
    audience_rating:Option[String], 
    audience_score:Int){
}
	
case class TomatoesAlternateIds(imdb: String){
}




//case class Foo(bar: String, baz: Int)
//val serialized: String = generate(Foo("bar", 42))
//val deserialized: Foo = parse[Foo](serialized)
