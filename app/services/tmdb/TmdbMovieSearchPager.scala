package services.tmdb

case class TmdbMovieSearchPager(
	total_pages:Int,
	total_results:Int,
	page:Int,
	results:List[TmdbMovieSearch]
) {

}
