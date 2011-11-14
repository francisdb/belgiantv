package services;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.BelgacomChannel;
import models.BelgacomMovie;
import models.BelgacomProgram;
import models.BelgacomResult;

import org.apache.commons.lang.StringEscapeUtils;

import play.Logger;
import play.libs.WS;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class BelgacomReader {

	private static final String BASE = "http://sphinx.skynet.be/tv-overal/ajax";
	private static final String LANG_VLAAMS = "lg-fl";
	private static final String LANG_NEDERLANDS = "lg-nl";

	public List<BelgacomChannel> read(final Date date){
		List<BelgacomChannel> channels = new ArrayList<BelgacomChannel>();
		channels.addAll(searchLanguage(date, LANG_VLAAMS));
		channels.addAll(searchLanguage(date, LANG_NEDERLANDS));
		
		return channels;
	}
	
	public List<BelgacomMovie> readMovies(final Date date, final int dayOffset){
		String url = BASE + "/get-movies-data/" + dayOffset + "?_=" + date.getTime();
		Logger.info(url);
		JsonElement json = WS.url(url).get().getJson();
		Gson gson = new Gson();
		Type listType = new com.google.gson.reflect.TypeToken<List<BelgacomMovie>>() {}.getType();
		List<BelgacomMovie> movies = gson.fromJson(json, listType);
		for(BelgacomMovie movie:movies){
			movie.title = StringEscapeUtils.unescapeHtml(movie.title);
		}
		return movies;
	}

	private List<BelgacomChannel> searchLanguage(final Date date, final String language) {
		List<BelgacomChannel> channels = new ArrayList<BelgacomChannel>();
		
		int page = 1;
		int maxPage = Integer.MAX_VALUE;
		while(page <= maxPage){
			BelgacomResult result = search(date, language, 8, page);
			for(BelgacomChannel channel:result.data){
				if(channel.channelId != null && !channel.channelId.isEmpty() ){
					channels.add(channel);
					for(BelgacomProgram program:channel.pr){
						program.title = StringEscapeUtils.unescapeHtml(program.title);
					}
				}
			}
			maxPage = result.maxPage;
			page++;
		}
		return channels;
	}

	private BelgacomResult search(final Date date, final String language, final int channelsPerPage, final int page) {
		int dayOffset = 0;
		String url = BASE + "/get-channel-data/"+channelsPerPage+"/"+page+"/"+language+"/"+dayOffset+"?_=" + date.getTime();
		Logger.info(url);
		JsonElement json = WS.url(url).get().getJson();
		Gson gson = new Gson();
		BelgacomResult result = gson.fromJson(json, BelgacomResult.class);
		return result;
	}
}
