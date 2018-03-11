package de.reiswich.homeautomation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * Ping the iPhone each x minutes. After it finds the iPhone, observers are
 * informed.
 * 
 * @author ereiswich
 * 
 */
public class DetectIPhoneTask extends TimerTask {
	private Logger logger = Logger.getLogger(DetectIPhoneTask.class.getName());

	private Set<IPhoneObserver> iPhoneObserver = new HashSet<IPhoneObserver>();

	protected void addIPhoneObserver(IPhoneObserver observer) {
		iPhoneObserver.add(observer);
	}

	@Override
	public void run() {
		try {
			//Bluetooth-MAC-Adresse ist die richtige
			Process process = Runtime.getRuntime().exec(
					"sudo l2ping -c 1 18:F6:43:21:FF:B3");
			process.waitFor();
			InputStream in = process.getInputStream();
			InputStreamReader inReader = new InputStreamReader(in);
			BufferedReader bufReader = new BufferedReader(inReader);

			// erste Zeile langt, um zu erkennen, ob iPhone online oder offline
			// ist
			String line = bufReader.readLine();
			if (line != null) {
				if (line.startsWith("Ping: 18:F6:43:21:FF:B3")) {
					logger.debug("... scanning: iPhone detected");
					handleIPhoneOnline();
				} else if (line.startsWith("Can't connect")) {
					logger.debug("... scanning: iPhone not found");
					handleIPhoneOffline();
				}
			} else {
				logger.debug("Ping return line is null");
				handleIPhoneOffline();
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	private void handleIPhoneOnline() {
		for (IPhoneObserver observer : iPhoneObserver) {
			logger.debug("Inform observer iPhone found: " + observer.toString());
			observer.iPhoneDetected();
		}
	}

	private void handleIPhoneOffline() {
		for (IPhoneObserver observer : iPhoneObserver) {
			observer.iPhoneOffline();
		}
	}

	@Override
	public String toString() {
		return "Detect iPhone scanner task";
	}
}
