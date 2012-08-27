package models.helper

import org.codehaus.jackson.annotate.JsonProperty
import java.util.Date
import org.codehaus.jackson.annotate.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
case class BelgacomProgram(

  @JsonProperty("pid") programId: String,

  @JsonProperty("pkey") programKey: String,

  title: String,

  @JsonProperty("sts") startTimeSeconds: Long,

  @JsonProperty("ets") endTimeSeconds: Long 
  
  //public int idx; // visualisation helper index
  //public int lenpx; // visualisation helper length in pixels
  //public int hr; // obsolete start hour
  //public int min; // obsolete start minutes
  //public int ehr; // obsolete end hour
  //public int emin; // obsolete end minutes
  //public String seo; // slug
  //public String hour; // obsolete start hour:minutes
  
  ) {
  
  def getProgramUrl() = {
    "http://sphinx.skynet.be/tv-overal/tv-gids/programma/" + programKey
  }

  def getStart() = {
    new Date(startTimeSeconds * 1000)
  }

  def getEnd() = {
    new Date(endTimeSeconds * 1000)
  }
}