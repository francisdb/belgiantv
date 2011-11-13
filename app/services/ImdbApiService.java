package services;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import models.ImdbApiMovie;
import play.Logger;
import play.libs.WS;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class ImdbApiService {
	
	public ImdbApiMovie readImdb(final String title){
		return readImdb(title, null);
	}

	public ImdbApiMovie readImdb(final String title, final Integer year){
		if(title==null){
			throw new IllegalArgumentException("NULL title");
		}
		String url = null;
		try {
			url = "http://www.imdbapi.com/?t=" + URLEncoder.encode(title, "UTF-8");
			if(year != null){
				url = url + "&y=" +year;
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		Logger.info(url);
		JsonElement json = WS.url(url).get().getJson();
		
		Gson gson = new Gson();
		ImdbApiMovie imdbMovie = gson.fromJson(json, ImdbApiMovie.class);
		return imdbMovie;
	}
}
