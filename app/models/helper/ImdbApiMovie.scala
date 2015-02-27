package models.helper

import play.api.Logger

case class ImdbApiMovie(
  imdbID: String,
	Title: String,
	imdbRating: String,
	imdbVotes: String,
	Runtime: String,
	Poster: String,
	Genre: String,
	Plot: String,
	Year: String,
	Released: String,
	Director: String
){

  val id = imdbID
  val title = Title
  val rating = imdbRating
  val votes = imdbVotes
  val runtime = Runtime
  val poster = Poster
  val genre = Genre
  val plot = Plot
  val year = Year.toInt
  val released = Released
  val director = Director
	
  lazy val getPercentRating = {
		if(rating == null){
			-1
		}else{
			try{
				(rating.toDouble * 10).toInt
			} catch {
			  case e:NumberFormatException => 
			    Logger.warn(e.getMessage)
				-1
			}
		}
		
	}
	
	lazy val getDecimalRating = {
		getPercentRating / 10
	}
	
	lazy val getUrl = "http://www.imdb.com/title/" + id
}