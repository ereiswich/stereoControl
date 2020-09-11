package de.reiswich.homeautomation.stereo_control.stereo;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialized radio playing using MPC play command.
 * 
 * @author reiswich
 * 
 */
public class MPCRadioPlayer_old {

	Logger logger = LoggerFactory.getLogger(MPCRadioPlayer_old.class.getName());

	public void playRadio() {
		try {

			ProcessBuilder builder = new ProcessBuilder("bash", "-c", "mpc volume 90 | mpc play 1");
			Process osProcess = builder.start();
			int returnCode = osProcess.waitFor();
			logger.info("MPC play 1 command sent to AVR");

			if (returnCode != 0) { // 0 ist per Konvention kein Fehler
				logger.debug("Mpc Play 1 hat einen Fehler zurückgeliefert. Code ist: " + returnCode);
				logger.debug("Starte MPD neu und versuche es noch mal");

				// Starte MPD neu und probier's noch mal
				builder = new ProcessBuilder("bash", "-c", "sudo systemctl restart mpd");
				osProcess = builder.start();

				playRadio();

			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
