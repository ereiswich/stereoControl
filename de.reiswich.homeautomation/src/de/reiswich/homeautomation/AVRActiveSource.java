package de.reiswich.homeautomation;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Send a signal to AV-Receiver to set the active source to RPi. That means, the
 * AV-Receiver will play the sound the RPi produces.
 * 
 * @author reiswich
 * 
 */
public class AVRActiveSource {
	Logger logger = Logger.getLogger(AVRActiveSource.class.getName());

	protected void aquireActiveSource() {
		try {
			Runtime r = Runtime.getRuntime();
			Process p = r
					.exec("python /home/pi/projects/radioplay/ActiveSourceCommand.py");
			p.waitFor();

			logger.info("Active Source command sent to AVR");
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

}
