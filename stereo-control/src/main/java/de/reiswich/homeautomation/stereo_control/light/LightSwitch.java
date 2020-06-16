package de.reiswich.homeautomation.stereo_control.light;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightSwitch {

	Logger logger = LoggerFactory.getLogger(LightSwitch.class.getName());
	
	public LightSwitch() {
	}

	public void switchOnLights() {
		if (isTimeToSwitchOnLights()) {
			logger.debug("Time to swith on lights = true");
			try {
				Runtime runtime = Runtime.getRuntime();
				runtime.exec("tdtool --on 1");
				logger.info("switching on light: 1");

				runtime.exec("tdtool --on 2");
				logger.info("switching on light: 2");

				runtime.exec("tdtool --on 3");
				logger.info("switching on light: 3");
			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		} else {
			logger.debug("Time to swith on lights = false");
		}
	}
	
	public boolean isTimeToSwitchOnLights() {
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setTime(new Date());
		boolean sommerzeit = calendar.getTimeZone().useDaylightTime();

		int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		if (sommerzeit) {
			logger.info("Es ist Sommerzeit. Nach 20 Uhr Licht einschalten.");
			// im sommer erst ab ca. 20 Uhr einschalten
			return hourOfDay >= 20;
		} else {
			logger.info("Es ist Winterzeit. Nach 18 Uhr Licht einschalten.");
			return hourOfDay >= 18;
		}
	}
}
