package services;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import models.TmdbMovie;
import play.Logger;
import play.Play;
import play.libs.WS;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class TmdbApiService {
	
	private static final String LANG_EN = "en";
	private static final String TYPE_JSON = "json";
	//private static final String TYPE_YAML = "yaml";
	//private static final String TYPE_XML = "xml";
	
	private static final String BASE = "http://api.themoviedb.org/2.1";
	private final String apiKey;
	
	public TmdbApiService() {
		this.apiKey = Play.configuration.getProperty("tmdb.apikey");
	}
	
	public TmdbMovie findOrRead(final String title){
		return findOrRead(title, null);
	}

	public TmdbMovie findOrRead(final String title, final Integer year){
		if(title==null){
			throw new IllegalArgumentException("NULL title");
		}
		TmdbMovie tmdbMovie = TmdbMovie.findByTitleAndYear(title, year);
		if(tmdbMovie == null){
			try{
				TmdbMovie[] result = search(title, year);
				tmdbMovie = result[0];
				tmdbMovie.save();
			}catch(Exception ex){
				Logger.error(ex.getMessage(), ex);
			}
		}
		return tmdbMovie;
	}
	
	
	private TmdbMovie[] search(final String title){
		return this.search(title, null);
	}
	
	private TmdbMovie[] search(final String title, final Integer year){
		String search = title;
		if(year != null){
			search = search + " " + year;
		}
		String encodedSearch = urlEncodeUtf8(search);
		// remove question mark ast this doesn't work
		encodedSearch = encodedSearch.replace("%3F", "");
		
		String url = BASE + "/Movie.search/" + LANG_EN + "/" + TYPE_JSON + "/" + apiKey + "/" + encodedSearch;
		Logger.info(url);
		JsonElement element = WS.url(url)
				.setHeader("Accept","application/json").get().getJson();
		
		if(element.isJsonArray() && element.getAsJsonArray().get(0).isJsonPrimitive()){
			throw new RuntimeException(element.getAsJsonArray().get(0).getAsString());
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//System.out.println(gson.toJson(element));
		return gson.fromJson(element, TmdbMovie[].class);
	}
	
	String getToken(){
		String url = BASE+"/Auth.getToken/"+TYPE_JSON+"/" + apiKey;
		JsonElement element = WS.url(url)
			.setHeader("Accept","application/json").get().getJson();
		return element.getAsJsonObject().get("token").getAsString();
	}
	
	String auth(final String token){
		// redirect here
		String url = "http://www.themoviedb.org/auth/" + token;
		return url;
		
	}

	private String urlEncodeUtf8(final String string) {
		try {
			return URLEncoder.encode(string, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
