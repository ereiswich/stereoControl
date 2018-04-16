package de.reiswich.homeautomation.stereo_control.light;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SunsetService {
	private final String HAMBURG_LATITUDE = "53.5534074";
	private final String HAMBURG_LONGITUDE = "9.9921962";

	private Logger logger = LoggerFactory.getLogger(SunsetService.class);

	public boolean isTimeToSwitchOnLights() {
		LocalTime sunsetTime = getSunsetTime();
		if (sunsetTime != null) {
		/*
		 * Da es schon vor dem Sonnenuntergang dunkel wird, schalte das Licht
		 * bereits 1 Stunde vor dem Sonnenuntergang ein.
		 */
		sunsetTime.minusHours(1);
			logger.info("Sunset service available. Today's Sunset: "
					+ sunsetTime);
			return LocalTime.now().isAfter(sunsetTime);
		} else {
			logger.info("Sunset service DOWN. Use default values");
			return isTimeToSwitchOnLightsUsingDefaults();
		}
	}

	protected LocalTime getSunsetTime() {
		try {
			InputStream jsonSunsetTime = connectToSunsetService();

			return getTimeFromJson(jsonSunsetTime);

		} catch (MalformedURLException e) {
			e.printStackTrace();
			logger.error("Sunset URL format error. \n" + e.getMessage());
		} catch (IOException e) {
			logger.error("Sunset URL connection error. \n" + e.getMessage());
			e.printStackTrace();
		} catch (ParseException e) {
			logger.error("Sunset parse error. \n" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	protected LocalTime getTimeFromJson(InputStream jsonSunsetTime) throws UnsupportedEncodingException, IOException, ParseException {
		JSONParser parser = new JSONParser();
		JSONObject sunsetObj = (JSONObject)parser.parse(new InputStreamReader(jsonSunsetTime, "UTF-8"));

		JSONObject result = (JSONObject) sunsetObj.get("results");
		String sunsetTimeString = (String)result.get("sunset");
		
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
		LocalDateTime sunsetTime = LocalDateTime.parse(sunsetTimeString, formatter);

		ZoneId berlin = ZoneId.of("Europe/Berlin");
		
		ZonedDateTime berlinDateTime = ZonedDateTime.of(sunsetTime, berlin);
				
		return berlinDateTime.toLocalTime();
	}

	private InputStream connectToSunsetService() throws MalformedURLException, IOException {
		URL sunsetURL = new URL(composeSunsetURL());
		URLConnection connection = sunsetURL.openConnection();
		InputStream jsonSunsetTime = connection.getInputStream();
		return jsonSunsetTime;
	}

	private boolean isTimeToSwitchOnLightsUsingDefaults() {
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setTime(new Date());
		boolean sommerzeit = calendar.getTimeZone().useDaylightTime();

		int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		if (sommerzeit) {
			// im sommer erst ab ca. 20 Uhr einschalten
			return hourOfDay >= 20;
		} else
			return hourOfDay >= 19;
	}

	/*
https://api.sunrise-sunset.org
	 */
	protected String composeSunsetURL() {
		String sunSetURLComplete = "https://api.sunrise-sunset.org/json?"
				+ "lat="+HAMBURG_LATITUDE + "&lng=" + HAMBURG_LONGITUDE + "&date=today&formatted=0";
		return sunSetURLComplete;
	}

}
