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
			Runtime osRuntime = Runtime.getRuntime();
			// man braucht den ProcessBuilder mit bash, da der | von der Command-Shell interpretiert wird
			ProcessBuilder builder = new ProcessBuilder("bash", "-c", "echo as | cec-client -s"); 
			Process osProcess = builder.start();
			// Process osProcess= osRuntime.exec("python3 /home/pi/projects/radioplay/ActiveSourceCommand.py");
			
			// Process osProcess = osRuntime.exec("echo as | cec-client -s");
			osProcess.waitFor();
			logger.info("Active Source CEC-Command executed");
			
			String mpcVolumeCommand ="mpc volume 90";
			osProcess = osRuntime.exec(mpcVolumeCommand);
			
			String mpcPlayCommand = "mpc play 1";
			osProcess = osRuntime.exec(mpcPlayCommand);
			int returnCode = osProcess.waitFor();
			if (returnCode != 0) { // 0 ist per Konvention kein Fehlerr
				logger.debug("Mpc Play 1 hat einen Fehler zurückgeliefert. Code ist: " + returnCode);
				logger.debug("Starte MPD neu und versuche es noch mal");
				// Starte MPD neu und probier's noch mal
				Process restartMpdProcess = osRuntime.exec("sudo systemctl restart mpd");
				restartMpdProcess.waitFor();
				osRuntime.exec(mpcPlayCommand);
			}
			logger.debug("Mpc play command executed");

//			InputStream pythonInputStream = p.getInputStream();
//			BufferedReader reader = new BufferedReader(new InputStreamReader(pythonInputStream));
//			String line;
//			while ((line = reader.readLine()) != null) {
//				logger.debug("Python output: " + line);
//			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Unerwarteter Fehler ist aufgetreten: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
