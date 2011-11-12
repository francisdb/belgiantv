package models;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;

public class Queue extends Model {
	
	@Id(Generator.UUID)
	public long id;
	
	public String url;
	
	public Movie movie;
	
	public static Query<Queue> all() {
        return Model.all(Queue.class);
    }
}
