package models;

import java.util.Date;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;

public class Movie extends Model{
	
	@Id(Generator.UUID)
	public String id;

	public String title;
	public String url;
	public String channel;
	public Date start;
	public Date end;
	
	public ImdbApiMovie imdb;
	
	static Query<Movie> all() {
        return Model.all(Movie.class);
    }
	
    public static Movie findByUrl(String url) {
        return all().filter("url", url).get();
    }
}
