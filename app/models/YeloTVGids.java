package models;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class YeloTVGids {
	@SerializedName("Succes")
	public String succes;
	
	@SerializedName("Result")
	public Result result;
	
	public static class Result{
		@SerializedName("IsToday")
		public boolean isToday;
		
		@SerializedName("ViewStart")
		public String viewStart;
		
		@SerializedName("ViewEnd")
		public String viewEnd;
		
		@SerializedName("Channels")
		public String channelsHtml;
		
		@SerializedName("Events")
		public String eventsHtml;
		
		@SerializedName("Meta")
		public Meta meta;
	}
	
	public static class Meta{
		//public schedules;
		public Map<String, Broadcast> pvrbroadcasts;
	}
	
	public static class Broadcast{
		public String event_id;
		public String code;
		public int is_serie;
		public String title;
		public long start_time;
		public long end_time;
		public String channel_webpvr_id;
	}
	
//	
//	 <li channel="1"><img src="http://img.yelo.be/uploads/channels/detail/MM067.png?v=0.1.11" alt="VTM" class="thumb_channel" width="100" height="50" border="0" /></li>
//     <li channel="2"><img src="http://img.yelo.be/uploads/channels/detail/MM065.png?v=0.1.11" alt="één" class="thumb_channel" width="100" height="50" border="0" /></li>
//     <li channel="3"><img src="http://img.yelo.be/uploads/channels/detail/MM06A.png?v=0.1.11" alt="VT4" class="thumb_channel" width="100" height="50" border="0" /></li>
//     <li channel="4"><img src="http://img.yelo.be/uploads/channels/detail/MM066.png?v=0.1.11" alt="Ketnet/Canvas" class="thumb_channel" width="100" height="50" border="0" /></li>
//     <li channel="5"><img src="http://img.yelo.be/uploads/channels/detail/MM068.png?v=0.1.11" alt="2BE" class="thumb_channel" width="100" height="50" border="0" /></li>
//     <li channel="6"><img src="http://img.yelo.be/uploads/channels/detail/MM084.png?v=0.1.11" alt="VijfTV" class="thumb_channel" width="100" height="50" border="0" /></li>
//     <li channel="377"><img src="http://img.yelo.be/uploads/channels/detail/MM169.png?v=0.1.11" alt="Njam!" class="thumb_channel" width="100" height="50" border="0" /></li>
//     <li channel="258"><img src="http://img.yelo.be/uploads/channels/detail/MM086.png?v=0.1.11" alt="Acht" class="thumb_channel" width="100" height="50" border="0" /></li>
//     <li channel="7"><img src="http://img.yelo.be/uploads/channels/detail/MM06B.png?v=0.1.11" alt="Vitaya" class="thumb_channel" width="100" height="50" border="0" /></li>
//     <li channel="283"><img src="http://img.yelo.be/uploads/channels/detail/MM0A0.png?v=0.1.11" alt="vtmKzoom" class="thumb_channel" width="100" height="50" border="0" /></li>
//     <li channel="369"><img src="http://img.yelo.be/uploads/channels/detail/MM0A3.png?v=0.1.11" alt="Exqi Sport-Cult" class="thumb_channel" width="100" height="50" border="0" /></li>
//     <li channel="511"><img src="http://img.yelo.be/uploads/channels/detail/MM1CD.png?v=0.1.11" alt="MENT TV" class="thumb_channel" width="100" height="50" border="0" /></li>
//     <li channel="389"><img src="http://img.yelo.be/uploads/channels/detail/MM0D9.png?v=0.1.11" alt="Be Shop TV" class="thumb_channel" width="100" height="50" border="0" /></li>
//     <li channel="22"><img src="http://img.yelo.be/uploads/channels/detail/MM072.png?v=0.1.11" alt="Ned1" class="thumb_channel" width="100" height="50" border="0" /></li>
//     <li channel="23"><img src="http://img.yelo.be/uploads/channels/detail/MM073.png?v=0.1.11" alt="Ned2" class="thumb_channel" width="100" height="50" border="0" /></li>
//     <li channel="24"><img src="http://img.yelo.be/uploads/channels/detail/MM074.png?v=0.1.11" alt="Ned3" class="thumb_channel" width="100" height="50" border="0" /></li>
}
