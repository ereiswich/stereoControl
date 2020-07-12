package de.reiswich.homeautomation.stereo_control.stereo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
			Process p = r.exec("python3 /home/pi/projects/radioplay/ActiveSourceCommand.py");
			// p.waitFor();

			InputStream pythonInputStream = p.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(pythonInputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				logger.debug("Python output: " + line);
			}
			logger.info("ActiveSourceCommand.py executed");
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Unerwarteter Fehler ist aufgetreten: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
