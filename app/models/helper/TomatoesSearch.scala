package models.helper

import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnoreProperties}

@JsonIgnoreProperties(ignoreUnknown = true)
case class TomatoesSearch(
    total:Int, 
    movies:List[TomatoesMovie]) {	
}

@JsonIgnoreProperties(ignoreUnknown = true)
case class TomatoesMovie(
    id:Int, 
    title:String,
    // annoying, this field can be 1999 or ""
    year:Option[String],
    runtime:String,
    ratings:TomatoesRatings, 
    alternate_ids:Option[TomatoesAlternateIds],
    critics_consensus:Option[String]) {

  def yearAsInt = year.flatMap(parseInt(_))

  private def parseInt(s: String) = try { Some(s.toInt) } catch { case _ => None }
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