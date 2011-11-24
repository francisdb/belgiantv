package controllers;

import java.util.Calendar;

import models.ImdbApiMovie;
import models.Movie;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import services.YeloReader;

public class TmdbTrigger extends Controller{
	
	private static final int DAYS_TO_FETCH = 8;

	@Before
	public static void log(){
		Logger.info("Trigger call:" + request.url);
	}

	

	

	public static void yelo() {
		YeloReader reader = new YeloReader();
		Calendar cal = Calendar.getInstance();
		for(int i = 0;i < DAYS_TO_FETCH;i++){
			reader.read(cal.getTime());
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		renderText("Yelo fetching done for next %s days", DAYS_TO_FETCH);
	}

	public static void clear() {
		Movie.all().delete();
		ImdbApiMovie.all().delete();
		renderText("Database cleared");
	}

}
