package services;

import models.TmdbMovie;
import models.TmdbPoster;

import org.junit.Ignore;
import org.junit.Test;

import play.test.UnitTest;

public class TmdbApiServiceTest extends UnitTest{

	@Test
	@Ignore("not needed for now")
	public void testGetToken(){
		TmdbApiService service = new TmdbApiService();
		String token = service.getToken();
		String url = service.auth(token);
		System.out.println(url);
	}
	
	@Test
	public void testSearch(){
		TmdbApiService service = new TmdbApiService();
		TmdbMovie result = service.findOrRead("Pulp Fiction", 1994);
		// call twice
		result = service.findOrRead("Pulp Fiction", 1994);
		System.out.println(result.name);
		for(TmdbPoster poster:result.posters){
			System.out.println(poster.image.url);
		}
	}

}
