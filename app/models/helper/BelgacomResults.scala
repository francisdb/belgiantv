package models.helper

case class BelgacomResult(
	currentPage:Int,
    maxPage:Option[Int],
	baseTimestamp:Long,
	data:List[BelgacomProgram]    
) {

}