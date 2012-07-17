package models.helper

import com.codahale.jerkson.Json._

case class TomatoesSearch(total:Int, movies:List[TomatoesMovie]) {	
}

case class TomatoesMovie(
    id:Int, 
    title:String, 
    year:String, 
    runtime:String, 
    ratings:List[TomatoesRatings], 
    alternate_ids:TomatoesAlternateIds,
    critics_consensus:String) {
}

case class TomatoesRatings (critics_rating:String, critics_score:Int, audience_rating:String, audience_score:Int){
}
	
case class TomatoesAlternateIds(imdb: String){
}

//case class Foo(bar: String, baz: Int)
//val serialized: String = generate(Foo("bar", 42))
//val deserialized: Foo = parse[Foo](serialized)