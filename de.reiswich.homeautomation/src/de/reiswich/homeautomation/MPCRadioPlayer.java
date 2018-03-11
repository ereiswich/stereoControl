package de.reiswich.homeautomation;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Initialized radio playing using MPC play command.
 * 
 * @author reiswich
 * 
 */
public class MPCRadioPlayer {

	Logger logger = Logger.getLogger(MPCRadioPlayer.class.getName());

	protected void playRadio() {
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
