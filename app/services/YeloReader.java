package services;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ImdbApiMovie;
import models.Movie;
import models.Queue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import play.Logger;
import play.libs.WS;
import play.libs.WS.HttpResponse;

public class YeloReader {

	private static final String BASE_URI = "http://yelo.be/";

	public List<Movie> read(final Date date){
    	
    	List<Movie> movies = new ArrayList<Movie>();
    	movies.addAll(readDay(date));
    	movies.addAll(readChannel("ned1", date));
    	movies.addAll(readChannel("ned2", date));
    	movies.addAll(readChannel("ned3", date));
    	
    	for(Movie movie:movies){
    		if(!sameDay(movie.start, date)){
    			throw new RuntimeException("Movie returned for wrong day" + movie.start + ", should be " + date);
    		}
    		movie.imdb = readOrFetch(movie);
    	}
    	
    	return movies;
	}

	private List<Movie> readChannel(final String channel, final Date date) {
		DateFormat dayFormat = new SimpleDateFormat("dd-MM-yyyy");
		String day = dayFormat.format(date);
		String guideUri = BASE_URI + "zenders/"+channel+"?date="+day;
		Document doc = readUri(guideUri);
    	
    	List<Movie> movies = new ArrayList<Movie>();
    	
    	Elements links = doc.select("a[class][href~=(/film/).*");
    	for(Element link:links){
    		Movie movie = Movie.findByUrl(link.absUrl("href"));
    		if(movie == null){
    			movie = parseChannelPageMovie(link);
    			movie.save();
    		}
    		movies.add(movie);
    	}
		return movies;
	}
	
	private List<Movie> readDay(final Date date) {
		List<Movie> movies = new ArrayList<Movie>();
		movies.addAll(readDayPart(date, 1));
		movies.addAll(readDayPart(date, 2));
		return movies;
	}
	
	private List<Movie> readDayPart(final Date date, final long part) {
		DateFormat dayFormat = new SimpleDateFormat("dd-MM-yyyy");
		String day = dayFormat.format(date);
		String guideUri = BASE_URI + "tv-gids?date="+day+"&part="+part;
		Document doc = readUri(guideUri);
    	
    	List<Movie> movies = new ArrayList<Movie>();
    	
    	Elements links = doc.select("a[href~=(/film/).*");
    	for(Element link:links){
    		Movie movie = Movie.findByUrl(link.absUrl("href"));
    		if(movie == null){
    			movie = parseGuidePageMovie(link);
    			movie.save();
    		}
    		movies.add(movie);
    	}
		return movies;
	}

	
	private Movie parseGuidePageMovie(Element link){
    	DateFormat hourFormat = new SimpleDateFormat("HH:mm");
    	DateFormat dayFormat = new SimpleDateFormat("dd-MM-yyyy");
		Element em = link.siblingElements().select("em").first();
		
		Movie movie = new Movie();
		movie.title = link.text();
		URI movieUri;
		try {
			movieUri = new URI(link.absUrl("href"));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		Map<String,String> params = splitParameters(movieUri);
		movie.url = movieUri.toString();
		movie.channel = params.get("channel");
		try {
			movie.start = dayFormat.parse(params.get("date"));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		try {
			Date hour = hourFormat.parse(em.text());
			movie.start = TimeUtil.merge(movie.start, hour);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return movie;
	}
	
	private Movie parseChannelPageMovie(Element link){
		DateFormat hourFormat = new SimpleDateFormat("HH:mm");
		DateFormat dayFormat = new SimpleDateFormat("dd-MM-yyyy");
		Element em = link.parent().siblingElements().select("span[class=datum]").first();
		
		Movie movie = new Movie();
		URI movieUri;
		try {
			movieUri = new URI(link.absUrl("href"));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		movie.title = link.attr("title").replace("FILM OP 2: ", "").replace("Z@ppbios: ", "");
		Map<String,String> params = splitParameters(movieUri);
		try {
			movie.start = dayFormat.parse(params.get("date"));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		movie.url = movieUri.toString();
		movie.channel = params.get("channel");
		try {
			Date hour = hourFormat.parse(em.text());
			movie.start = TimeUtil.merge(movie.start, hour);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return movie;
	}
	

	private boolean sameDay(Date day1, Date day2) {
		Calendar day1Cal = Calendar.getInstance();
		day1Cal.setTime(day1);
		Calendar day2Cal = Calendar.getInstance();
		day2Cal.setTime(day2);
		return day1Cal.get(Calendar.YEAR) == day2Cal.get(Calendar.YEAR) &&
			day1Cal.get(Calendar.MONTH) == day2Cal.get(Calendar.MONTH) &&
			day1Cal.get(Calendar.DAY_OF_MONTH) == day2Cal.get(Calendar.DAY_OF_MONTH);
	}
	
	private Document readUri(String uri) {
		Logger.info(uri);
    	HttpResponse response = WS.url(uri).get();
    	String html = response.getString();
    	Document doc = Jsoup.parse(html, BASE_URI);
		return doc;
	}
	
	private Map<String,String> splitParameters(URI uri){
		Map<String,String> result = new HashMap<String, String>();
		String query = uri.getQuery();
		String[] params = query.split("&");
		for(String param:params){
			String[] keyValue = param.split("=");
			result.put(keyValue[0], keyValue[1]);
		}
		return result;
	}
	
	private ImdbApiMovie readOrFetch(final Movie movie){
		ImdbApiMovie imdbMovie = ImdbApiMovie.findByTitle(movie.title);
		if(imdbMovie == null){
			Queue queue = new Queue();
			queue.movie = movie;
			queue.save();
			System.err.println(queue);
		}else{
			movie.imdb = imdbMovie;
			movie.save();
		}
		return imdbMovie;
	}
}
