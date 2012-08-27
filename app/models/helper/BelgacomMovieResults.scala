package models.helper

case class BelgacomMovieResults(
    data:List[BelgacomMovie],
    baseTimestamp:Long,
    currentPage: Option[Int],
    maxPage:Int
) {
  

}