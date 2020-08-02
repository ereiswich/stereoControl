package de.reiswich.homeautomation.stereo_control.stereo;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
 
	private final long timeout = 10;

	public void aquireActiveSource() {
		try {
			Runtime osRuntime = Runtime.getRuntime();
			// man braucht den ProcessBuilder mit bash, da der '|' von der Command-Shell
			// interpretiert wird
			ProcessBuilder builder = new ProcessBuilder("bash", "-c", "echo as | cec-client -s");
			Process osProcess = builder.start();

			osProcess.waitFor(timeout, TimeUnit.SECONDS);
			logger.info("Active Source CEC-Command executed");


		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Unerwarteter Fehler ist aufgetreten: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
