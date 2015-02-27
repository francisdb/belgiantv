package models.helper

case class BelgacomChannel(
  cid:String,
  cname:String,
	cnum:String,
	clogo:String,
	pr:List[BelgacomProgram]    
) {

  val channelId = cid
  val channelName = cname
  val channelNumber = cnum
  val channelLogo = clogo

  override def toString = s"$channelName [${pr.size}]"
}