package de.reiswich.homeautomation.stereo_control.scanning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pingt mobile Geräte regelmäßig via Bluetooth an.
 * Informiert Observer, wenn ein Gerät gefunden oder nicht gefunden wird.
 */
public class ScanIPhoneTask implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScanIPhoneTask.class);
	private static final int PING_TIMEOUT_SECONDS = 10;

	private final Set<IPhoneObserver> observers = new CopyOnWriteArraySet<>();
	private final Properties mobileDevices;

	public ScanIPhoneTask(Properties mobileDevices) {
		this.mobileDevices = mobileDevices;
	}

	public void addIPhoneObserver(IPhoneObserver observer) {
		observers.add(observer);
	}

	@Override
	public void run() {
		LOGGER.debug("Starte iPhone-Scan...");

		boolean anyDeviceDetected = mobileDevices.stringPropertyNames().stream()
			.anyMatch(deviceOwner -> pingMobileDevice(
				deviceOwner,
				mobileDevices.getProperty(deviceOwner)
			));

		if (anyDeviceDetected) {
			notifyIPhoneOnline();
		} else {
			notifyIPhoneOffline();
		}
	}

	private boolean pingMobileDevice(String deviceOwner, String macAddress) {
		Process process = null;
		try {
			String command = "sudo hcitool name " + macAddress;
			process = Runtime.getRuntime().exec(command);

			boolean finished = process.waitFor(PING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			if (!finished) {
				LOGGER.warn("Ping-Timeout für Gerät: {}", deviceOwner);
				return false;
			}

			try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
				String line = reader.readLine();
				if (line != null && !line.isEmpty()) {
					LOGGER.debug("Gerät von {} erkannt", deviceOwner);
					return true;
				}
				LOGGER.debug("Keine Antwort für Gerät von {}", deviceOwner);
				return false;
			}

		} catch (IOException e) {
			LOGGER.error("IO-Fehler beim Ping: {}", e.getMessage());
			return false;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.warn("Ping unterbrochen für Gerät: {}", deviceOwner);
			return false;
		} finally {
			if (process != null) {
				process.destroyForcibly();
			}
		}
	}

	private void notifyIPhoneOnline() {
		observers.forEach(observer -> {
			LOGGER.debug("Informiere Observer über iPhone-Fund: {}", observer);
			observer.iPhoneDetected();
		});
	}

	private void notifyIPhoneOffline() {
		observers.forEach(IPhoneObserver::iPhoneOffline);
	}

	@Override
	public String toString() {
		return "ScanIPhoneTask";
	}
}
