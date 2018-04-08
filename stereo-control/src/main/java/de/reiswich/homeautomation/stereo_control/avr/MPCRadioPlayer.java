package de.reiswich.homeautomation.stereo_control.avr;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialized radio playing using MPC play command.
 * 
 * @author reiswich
 * 
 */
public class MPCRadioPlayer {

	Logger logger = LoggerFactory.getLogger(MPCRadioPlayer.class.getName());

	public void playRadio() {
		try {
			int radioStationNumber = determineRadioStationNumber();
			Process process = Runtime.getRuntime().exec(
					"mpc play " + radioStationNumber);
			process.waitFor();
			logger.info("MPC play " + radioStationNumber
					+ " - command sent to AVR");
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	private int determineRadioStationNumber() {
		// Calendar cal = new GregorianCalendar();
		// cal.setTime(new Date());
		// int hour = cal.get(Calendar.HOUR_OF_DAY);
		// ab 18 Uhr wird gechillt
		return 1;
		// if (hour >= 18) {
		// return 1; // ABC Lounge
		// } else {
		// return 3; // 917x.fm
		// }
	}
}
