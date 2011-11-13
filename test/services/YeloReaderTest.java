package services;

import org.junit.Test;

import play.test.UnitTest;

public class YeloReaderTest extends UnitTest{
	
	@Test
	public void testGetYear(){
		YeloReader reader = new YeloReader();
		Integer year = reader.getYear("http://yelo.be/film/prince-of-persia-the-sands-of-time");
		assertNotNull(year);
		assertEquals(Integer.valueOf("2010"), year);
	}

}
