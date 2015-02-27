package models.helper

case class BelgacomResults(
	currentPage: Option[Int],
  maxPage: Option[Int],
	baseTimestamp: Long,
  startTimestamp: Option[Long],
  endTimestamp: Option[Long],
  data: List[BelgacomProgram]
)