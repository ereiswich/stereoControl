package de.reiswich.homeautomation;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;
import static org.junit.Assert.*;

public class RadioControllerTest {

	@Test
	public void testIsTimeToPlay() {
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 23);
		assertTrue(isTimeToPlay(cal));

		cal.set(Calendar.HOUR_OF_DAY, 24);
		assertFalse(isTimeToPlay(cal));

		cal.set(Calendar.HOUR_OF_DAY, 9);
		assertFalse(isTimeToPlay(cal));

		cal.set(Calendar.HOUR_OF_DAY, 10);
		assertTrue(isTimeToPlay(cal));
	}

	private boolean isTimeToPlay(Calendar cal) {
		boolean timeToPlay = false;
		// 24 hour clock
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		// don't play at night
		if (23 >= hour && hour >= 10) {
			timeToPlay = true;
		}
		return timeToPlay;
	}

}
