package models.helper;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class BelgacomProgram {
	
	@SerializedName("pid")
	public String programId;
	
	@SerializedName("pkey")
	public String programKey;
	
	public String title;
	
	@SerializedName("sts")
	public long startTimeSeconds;
	
	@SerializedName("ets")
	public long endTimeSeconds;
	
	
	//public int idx; // visualisation helper index
	//public int lenpx; // visualisation helper length in pixels
	
	//public int hr; // obsolete start hour
	//public int min; // obsolete start minutes
	//public int ehr; // obsolete end hour
	//public int emin; // obsolete end minutes
	
	//public String seo; // slug
	//public String hour; // obsolete start hour:minutes
	
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
