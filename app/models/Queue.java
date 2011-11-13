package models;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Index;
import siena.Model;
import siena.NotNull;
import siena.Query;

public class Queue extends Model {
	
	@Id(Generator.UUID)
	public String id;
	
	@NotNull
	@Column("movie")
	@Index("movie_index")
	public Movie movie;
	
	public static Query<Queue> all() {
        return Model.all(Queue.class);
    }
	
	@Override
	public String toString() {
		return String.format("Queue for %s [%s]", movie.id, movie.url);
	}
}
