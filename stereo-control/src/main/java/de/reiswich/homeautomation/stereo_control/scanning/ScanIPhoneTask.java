package de.reiswich.homeautomation.stereo_control.scanning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ping the iPhone each x minutes. After it finds the iPhone, observers are
 * informed.
 *
 * @author ereiswich
 *
 */
public class ScanIPhoneTask extends TimerTask {
	private Logger logger = LoggerFactory.getLogger(ScanIPhoneTask.class.getName());

	private Set<IPhoneObserver> iPhoneObserver = new HashSet<IPhoneObserver>();

	private Properties _mobileDevices;

	public ScanIPhoneTask(Properties mobileDevices) {
		_mobileDevices = mobileDevices;
	}

	public void addIPhoneObserver(IPhoneObserver observer) {
		iPhoneObserver.add(observer);
	}

	@Override
	public void run() {
		logger.debug("Running Scan IPhones task...");
		List<Boolean> pingResults = new ArrayList<>();
		for (Entry<Object, Object> deviceKeySet : _mobileDevices.entrySet()) {
			boolean pingResult = pingMobileDevice((String) deviceKeySet.getKey(), (String) deviceKeySet.getValue());
			pingResults.add(pingResult);
		}

		boolean iPhoneDetected = pingResults.contains(true);

		if (iPhoneDetected) {
			handleIPhoneOnline();
		} else {
			handleIPhoneOffline();
		}
	}

	private boolean pingMobileDevice(String mobileDeviceOwner, String deviceMacAdress) {
		boolean pingResult = false;
		Process process = null;
		try {
			// Bluetooth-MAC-Adresse ist die richtige
			// String l2PingString = "sudo l2ping -c 1 " + deviceMacAdress;
			String hciToolPingString = "sudo hcitool name " + deviceMacAdress;
			process = Runtime.getRuntime().exec(hciToolPingString);
			boolean finished = process.waitFor(10, TimeUnit.SECONDS);
			if (finished) {
				try (InputStream in = process.getInputStream()) {
					InputStreamReader inReader = new InputStreamReader(in);
					BufferedReader bufReader = new BufferedReader(inReader);

					String line = bufReader.readLine();
					if (line != null && !line.isEmpty()) {
						pingResult = true;
						logger.debug("Mobile device for owner: {} detected", mobileDeviceOwner);
					} else {
						logger.debug("Ping for mobile device owner: {} return line is: {} ", mobileDeviceOwner, line);
					}
				}

			} else {
				logger.warn("Ping timeout for device: " + mobileDeviceOwner);
				process.destroyForcibly();
			}

		} catch (IOException | InterruptedException e) {
			logger.error(e.getMessage());
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
		} finally {
			if (process != null) {
				process.destroyForcibly();
			}
		}
		return pingResult;
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
