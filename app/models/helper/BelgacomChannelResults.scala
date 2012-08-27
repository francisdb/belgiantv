package models.helper

case class BelgacomResult(
	currentPage:Int,
	maxPage:Int,
	baseTimestamp:Long,
	data:List[BelgacomChannel]    
) {

}