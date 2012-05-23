package models.helper;

import java.util.List;


import com.google.gson.annotations.SerializedName;

public class BelgacomChannel {
	
	@SerializedName("cid")
	public String channelId;
	
	@SerializedName("cname")
	public String channelname;
	
	@SerializedName("cnum")
	public String channelNumber;
	
	@SerializedName("clogo")
	public String channelLogo;
	
	public List<BelgacomProgram> pr;
	
	//public String liveid; // no idea
	//public String offset; // visualisation start offset in pixels?
	//public int idx; // visualisation index
	//public String fill; // visualisation fill?
}
