package de.reiswich.homeautomation.stereo_control.avr;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Send a signal to AV-Receiver to set the active source to RPi. That means, the
 * AV-Receiver will play the sound the RPi produces.
 * 
 * @author reiswich
 * 
 */
public class AVRActiveSource {
	Logger logger = LoggerFactory.getLogger(AVRActiveSource.class.getName());

	public void aquireActiveSource() {
		try {
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("python /home/pi/projects/radioplay/ActiveSourceCommand.py");
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
