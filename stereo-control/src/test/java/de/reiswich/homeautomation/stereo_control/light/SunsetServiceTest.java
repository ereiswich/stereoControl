package de.reiswich.homeautomation.stereo_control.light;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalTime;

import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import de.reiswich.homeautomation.stereo_control.light.SunsetService;

public class SunsetServiceTest {

	private SunsetService sunsetService;

	@Before
	public void setUp() {
		sunsetService = new SunsetService();
	}

	@Test
	public void composeSunsetURLTest() {
		// Arrange
		String url = "https://api.sunrise-sunset.org/json?lat=53.5534074&lng=9.9921962&date=today&formatted=0";

		// Act
		String sunsetURL = sunsetService.composeSunsetURL();

		// Assert
		assertEquals(sunsetURL, url);
	}

	@Test
	public void getTimeFromJson() throws UnsupportedEncodingException, IOException, ParseException {
		// Arrange
		InputStream in = this.getClass().getResourceAsStream("jsontime.json");

		// Act
		LocalTime sunsetTime = sunsetService.getTimeFromJson(in);

		// Assert
		assertEquals(LocalTime.of(16, 7, 10), sunsetTime);
	}
}
