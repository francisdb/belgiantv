package services;

import java.util.Date;
import java.util.List;

import models.BelgacomChannel;
import models.BelgacomMovie;
import models.BelgacomProgram;

import org.junit.Test;

import play.Logger;
import play.test.UnitTest;

public class BelgacomReaderTest extends UnitTest{

	@Test
	public void testRead() {
		BelgacomReader reader = new BelgacomReader();
		List<BelgacomChannel> channels = reader.read(new Date());
		
		Logger.info("%s channels", channels.size());
		for(BelgacomChannel channel:channels){
			Logger.info("%s %s programs", channel.channelname, channel.pr.size());
		}
		
		for(BelgacomProgram program:channels.get(0).pr){
			Logger.info("  %s - %s", program.getStart(), program.title);
		}
	}
	
	@Test
	public void testReadMovies(){
		BelgacomReader reader = new BelgacomReader();
		List<BelgacomMovie> movies = reader.readMovies(new Date(),1);
		Logger.info("%s movies", movies.size());
		for(BelgacomMovie movie:movies){
			Logger.info("%s - %s - %s %s %s", movie.getStart(), movie.channelname, movie.title, movie.bq, movie.btv);
		}
	}

}
