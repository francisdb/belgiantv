package services.proximus

case class BelgacomChannelListing(
  channels: Seq[BelgacomChannel]
)

case class BelgacomChannel(
  id: String,  // "UID50037",
  name: String //"name": "één",
//"callLetter": "één",
//"number": "1",
//"logo": {
//"Original": "https://experience-cache.proximustv.be:443/posterserver/poster/DEFAULT/t_0007_002.png",
//"custom": "https://experience-cache.proximustv.be:443/posterserver/poster/DEFAULT/%s/t_0007_002.png"
//},
//"language": "nl",
//"hd": false,
//"replayable": true,
//"ottReplayable": true,
//"playable": false,
//"ottPlayable": true,
//"recordable": false,
//"cloudRecordable": true,
//"subscribed": false,
//"categories": [
//"NPVR"
//],
//"catchUpWindowInHours": 36
) {
  override def toString = s"$name [$id]"
}
