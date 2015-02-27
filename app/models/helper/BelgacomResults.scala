package models.helper

case class BelgacomResults(
	currentPage: Int,
  maxPage: Option[Int],
	baseTimestamp: Long,
  startTimestamp: Long,
  endTimestamp: Long,
  data: List[BelgacomProgram]
) {

}