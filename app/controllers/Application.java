package controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Movie;
import models.Queue;
import play.Logger;
import play.mvc.Controller;
import services.YeloReader;

public class Application extends Controller {

    public static void index(Date date) {
    	Date day = new Date();
    	if(date != null){
    		day = date;
    	}
    	
    	YeloReader reader = new YeloReader();
    	
    	
    	List<Movie> movies = reader.read(day);
    	
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(day);
    	cal.add(Calendar.DAY_OF_WEEK, -1);
    	Date before = cal.getTime();
    	cal.setTime(day);
    	cal.add(Calendar.DAY_OF_WEEK, +1);
    	Date after = cal.getTime();
    	
    	long queue = Queue.all().count();
    	
        render(movies, day, before, after, queue);
    }

	public static void test() {
		Logger.info("test");
		ok();
	}

}