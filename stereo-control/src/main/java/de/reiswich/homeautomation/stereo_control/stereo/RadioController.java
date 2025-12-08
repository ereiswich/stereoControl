package de.reiswich.homeautomation.stereo_control.stereo;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.reiswich.homeautomation.stereo_control.scanning.IPhoneObserver;
import de.reiswich.homeautomation.stereo_control.scanning.ScanIPhoneTask;
import de.reiswich.homeautomation.stereo_control.stereo.api.DenonAvrController_Telnet;
import de.reiswich.homeautomation.stereo_control.stereo.api.IPlayerController;
import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosCommandResponse;

public class RadioController implements AutoCloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(RadioController.class);
	private static final ZoneId TIMEZONE_BERLIN = ZoneId.of("Europe/Berlin");

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
		r -> {
			Thread thread = new Thread(r, "iPhone-Scanner-Thread");
			thread.setDaemon(true);  // Daemon-Thread für sauberes Herunterfahren
			return thread;
		}
	);

	private volatile ScheduledFuture<?> currentScanTask;
	private final Object lock = new Object();

	private final Properties mobileDevices;
	private final IPlayerController playerController;
	private final DenonAvrController_Telnet denonAvrController;
	private final RadioControllerProperties radioControllerProperties;

	public RadioController(Properties props,
		IPlayerController playerController,
		DenonAvrController_Telnet denonAvrController,
		RadioControllerProperties radioControllerProperties) {
		this.mobileDevices = props;
		this.playerController = playerController;
		this.denonAvrController = denonAvrController;
		this.radioControllerProperties = radioControllerProperties;
		LOGGER.debug("RadioControllerProperties: {}", radioControllerProperties);
	}

	public void init() {
		startScanning();
	}

	@Override
	public void close() {
		LOGGER.info("Shutting down RadioController...");
		scheduler.shutdownNow();
		try {
			if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
				LOGGER.warn("Scheduler did not terminate within timeout");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.warn("Interrupted while waiting for scheduler shutdown");
		}
	}

	private void cancelCurrentTask() {
		synchronized (lock) {
			if (currentScanTask != null) {
				currentScanTask.cancel(false);
				currentScanTask = null;
			}
		}
	}

	private void scheduleScanTask(Runnable task) {
		long scanRateInSec = radioControllerProperties.getScanRateInSec();
		synchronized (lock) {
			currentScanTask = scheduler.scheduleAtFixedRate(
				task, 0, scanRateInSec, TimeUnit.SECONDS
			);
		}
		LOGGER.info("iPhone-Scanner initialisiert mit Scan-Rate: {} Sekunden", scanRateInSec);
	}

	private void startScanning() {
		ScanIPhoneTask scanTask = new ScanIPhoneTask(mobileDevices);
		scanTask.addIPhoneObserver(new InitialScanObserver());
		scheduleScanTask(scanTask);
	}

	private void startRadioPlayer() {
		LOGGER.info("Schalte Denon AVR ein...");
		String response = denonAvrController.turnOnAvr();
		LOGGER.info("Denon AVR Einschalt-Antwort: {}", response);

		int playerPid = radioControllerProperties.getPlayerPid();
		LOGGER.info("Starte Radio über HEOS-API mit Player-ID: {}", playerPid);
		HeosCommandResponse heosResponse = playerController.playRadio(playerPid);
		LOGGER.info("HEOS playRadio Antwort: {}", heosResponse);
	}

	private void stopRadioPlayer() {
		LOGGER.info("Stoppe Radio-Player...");
		try {
			HeosCommandResponse heosResponse = playerController.stopRadio(radioControllerProperties.getPlayerPid());
			LOGGER.info("HEOS stopRadio Antwort: {}", heosResponse);
		} catch (Exception e) {
			LOGGER.error("Fehler beim Stoppen des Radio-Players: {}", e.getMessage(), e);
		}
	}

	private void scheduleStopPlayingAndRestartScanning() {
		long restartMinutes = radioControllerProperties.getRestartAfterMinutes();
		scheduler.schedule(() -> {
			stopRadioPlayer();
			initRestartScanning();
		}, restartMinutes, TimeUnit.MINUTES);
		LOGGER.info("Radio-Stop Timer initialisiert: {} Minuten", restartMinutes);
	}

	private void initRestartScanning() {
		LOGGER.info("Initialisiere Restart-Scanner alle {} Sekunden",
			radioControllerProperties.getScanRateInSec());

		ScanIPhoneTask restartTask = new ScanIPhoneTask(mobileDevices);
		restartTask.addIPhoneObserver(new RestartScanObserver());
		scheduleScanTask(restartTask);
	}

	protected boolean isTimeToPlayMusic() {
		int currentHour = LocalTime.now(TIMEZONE_BERLIN).getHour();
		int startTime = radioControllerProperties.getStartTimeToPlayMusic();
		int endTime = radioControllerProperties.getEndTimeToPlayMusic();

		boolean timeToPlay = currentHour >= startTime && currentHour < endTime;
		LOGGER.debug("Zeit zum Musik abspielen: {} (aktuelle Stunde: {}, erlaubt: {} - {})",
			timeToPlay, currentHour, startTime, endTime);
		return timeToPlay;
	}

	@Override
	public String toString() {
		return RadioController.class.getSimpleName();
	}

	private class InitialScanObserver implements IPhoneObserver {
		@Override
		public void iPhoneDetected() {
			LOGGER.info("iPhone gefunden, stoppe Scan-Task. Prüfe ob Zeit zum Musik abspielen ist.");
			cancelCurrentTask();

			if (isTimeToPlayMusic()) {
				LOGGER.info("Zeit zum Musik abspielen = true, starte Radio");
				startRadioPlayer();
				scheduleStopPlayingAndRestartScanning();
			} else {
				LOGGER.info("Zeit zum Musik abspielen = false. Starte Scanning neu.");
				initRestartScanning();
			}
		}

		@Override
		public void iPhoneOffline() {
			LOGGER.trace("iPhone nicht erreichbar.");
		}
	}

	private class RestartScanObserver implements IPhoneObserver {
		private final AtomicInteger pingFailedCounter = new AtomicInteger(0);
		private final int maxPingFails = radioControllerProperties.getPingFailCounter();

		@Override
		public void iPhoneOffline() {
			int failCount = pingFailedCounter.incrementAndGet();
			LOGGER.debug("iPhone offline. Ping fail counter: {}", failCount);

			if (failCount >= maxPingFails) {
				LOGGER.info("Ping fail counter >= {}. Smartphone außer Reichweite, starte scanning neu.",
					maxPingFails);
				cancelCurrentTask();
				startScanning();
			}
		}

		@Override
		public void iPhoneDetected() {
			pingFailedCounter.set(0);
			LOGGER.trace("iPhone erkannt: Ping fail counter zurückgesetzt");
		}
	}
}
