package models.helper;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class BelgacomMovie {
	
	@SerializedName("pid")
	public long programId;
	
	@SerializedName("pkey")
	public String programKey;
	
	@SerializedName("ti")
	public String title;
	
	@SerializedName("lg")
	public String language;

	@SerializedName("su")
	public String summary;
	
	@SerializedName("chname")
	public String channelname;
	
	@SerializedName("chnumb")
	public String channelNumber;
	
	@SerializedName("chid")
	public int channelId;
	
	@SerializedName("chlogo")
	public String channelLogo;
	
	@SerializedName("sts")
	public long startTimeSeconds;
	
	@SerializedName("ets")
	public long endTimeSeconds;

	@SerializedName("img")
	public String image;
	
	@SerializedName("gr")
	int genreId;
	
	@SerializedName("grname")
	public String genre;
	
	public String bq; // ???
	public String btv; // belgacomtv?
	
	//public String pk; // duplicate of pkey
	//public String seo; // slug
	//public String shour; // obsolete start hour:minutes
	
	public String getProgramUrl(){
		return "http://sphinx.skynet.be/tv-overal/tv-gids/programma/" + programKey;
	}
	
	public Date getStart(){
		return new Date(startTimeSeconds * 1000);
	}
	
	public Date getEnd(){
		return new Date(endTimeSeconds * 1000);
	}
}
