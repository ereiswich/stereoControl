package de.reiswich.homeautomation.stereo_control;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.reiswich.homeautomation.stereo_control.stereo.MPCRadioPlayer;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RadioControllerTest {
	
	@Autowired
	private RadioController radioController;
	
	@Autowired private MPCRadioPlayer radioPlayer;

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
	
	@Test
	public void playMusicTest() {
		this.radioController.init();
	}
	
	@Test
	public void iPhoneDetectedTest() throws InterruptedException {
		 this.radioController.iPhoneDetected();
	}
	
	@After
	public void tearDown() {
		this.radioPlayer.stopPlaying();
	}

}
