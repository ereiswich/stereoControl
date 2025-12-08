package de.reiswich.homeautomation.stereo_control.stereo;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.reiswich.homeautomation.stereo_control.scanning.IPhoneObserver;
import de.reiswich.homeautomation.stereo_control.scanning.ScanIPhoneTask;
import de.reiswich.homeautomation.stereo_control.stereo.api.DenonAvrController_Telnet;
import de.reiswich.homeautomation.stereo_control.stereo.api.IPlayerController;
import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosCommandResponse;

public class RadioController {
	private static final Logger LOGGER = LoggerFactory.getLogger(RadioController.class.getName());

	private volatile Timer _scanIPhoneTimer;
	private volatile ScanIPhoneTask _scanIPhoneTask;
	private volatile ScanIPhoneTask _restartIPhoneScannerTask;
	private final Properties _mobileDevices;
	private final IPlayerController playerController;
	private final DenonAvrController_Telnet denonAvrController;
	private final RadioControllerProperties radioControllerProperties;

	public RadioController(Properties props,
		IPlayerController playerController,
		DenonAvrController_Telnet denonAvrController, RadioControllerProperties radioControllerProperties) {
		_mobileDevices = props;
		this.playerController = playerController;
		this.denonAvrController = denonAvrController;
		this.radioControllerProperties = radioControllerProperties;
		LOGGER.debug("RadioControllerproperties:  {}", radioControllerProperties);
	}

	public void init() {
		startScanning(0);
	}

	private void stopScanning() {
		// stop scanning
		_scanIPhoneTask.cancel();
		// remove scheduled "stop playing tasks" etc.
		_scanIPhoneTimer.purge();
	}

	private void startScanning(long delay) {
		_scanIPhoneTask = new ScanIPhoneTask(_mobileDevices);
		_scanIPhoneTask.addIPhoneObserver(new IPhoneObserver() {
			@Override
			public void iPhoneDetected() {
				LOGGER.info("\n \t >>> iPhone found, stop scanning task. Check if it's time to play music.");
				stopScanning();

				if (isTimeToPlayMusic()) {
					LOGGER.info("Time to play music = true, start playing radio");
					startRadioPlayer();
					initStopPlayingAndRestartScanningTask();

				} else {
					LOGGER.info("Time to play music = false. Restart scanning.");
					initRestartScanning();
				}
			}

			@Override
			public void iPhoneOffline() {
				LOGGER.trace("iPhone connection lost.");
			}
		});

		_scanIPhoneTimer = new Timer("iPhone Scanner");
		// scan every x-seconds
		long scanRateInMillis = TimeUnit.SECONDS.toMillis(radioControllerProperties.getScanRateInSec());
		_scanIPhoneTimer.schedule(_scanIPhoneTask, delay, scanRateInMillis);
		LOGGER.info("Initializing DetectIPhoneTask tasks with scan rate: {} seconds", radioControllerProperties.getScanRateInSec());
	}

	private void startRadioPlayer() {
		LOGGER.info("Switching on Denon AVR ...");
		String response = denonAvrController.turnOnAvr();
		LOGGER.info("Turn on command response from Denon AVR: {}", response);

		LOGGER.info("startRadioPlayer with HEOS-API and playerId: {}", this.radioControllerProperties.getPlayerPid());
		HeosCommandResponse heosCommandResponse = playerController.playRadio(radioControllerProperties.getPlayerPid());

		LOGGER.info("playRadio command response: {}", heosCommandResponse);
	}

	private void stopRadioPlayer() {
		LOGGER.info("try to stop RadioPlayer after {} Minutes.", radioControllerProperties.getRestartAfterMinutes());
		try {
			HeosCommandResponse heosCommandResponse = playerController.stopRadio(this.radioControllerProperties.getPlayerPid());
			LOGGER.info("stopRadio HEOS command response: {}", heosCommandResponse);

			// 08.12.2025: nervt doch zu doll beim TV gucken, wenn dieser ausgeschaltet wird.
			// String avrResponse = denonAvrController.turnOffAvr();
			//LOGGER.info("Turn Off command AVR response: {}", avrResponse);

		} catch (Exception e) {
			LOGGER.error("Failed to stop radio player   {}", e.getMessage());
		}
	}

	/*
	 * Stoppe radio nach 90 Minuten, damit es nicht die ganze Nacht durchläuft und starte scanning task.
	 */
	private void initStopPlayingAndRestartScanningTask() {
		StopRadioPlayingTask stopRadioPlaying = new StopRadioPlayingTask();
		stopRadioPlaying.addObserver(this::initRestartScanning);

		long minutesForRestartInMillis = TimeUnit.MINUTES.toMillis(radioControllerProperties.getRestartAfterMinutes());
		_scanIPhoneTimer.schedule(stopRadioPlaying, minutesForRestartInMillis);
		// // 60 Min.
		LOGGER.info("Stop playing radio timer initialized after: {} Min.", radioControllerProperties.getRestartAfterMinutes());
	}

	@Override
	public String toString() {
		return RadioController.class.getSimpleName();
	}

	/*
	 * As soon as iPhone connection is lost, the bluetooth device is no longer (=
	 * person not at home) in range, thus restart scanning.
	 */
	private void initRestartScanning() {
		this.stopRadioPlayer();

		LOGGER.info("Initializing restart iPhone scanner task every '{}' seconds", radioControllerProperties.getScanRateInSec());
		_restartIPhoneScannerTask = new ScanIPhoneTask(_mobileDevices);
		_restartIPhoneScannerTask.addIPhoneObserver(new IPhoneObserver() {
			int pingFailedCounter = 0;

			@Override
			public void iPhoneOffline() {
				pingFailedCounter++;
				LOGGER.debug("iPhone connection lost. Setting ping failed counter to: {}", pingFailedCounter);
				/*
				 * Ping may fail. Don't restart iPhone scanner immediately. Failing e.g. ten
				 * times is more unlikely, thus iPhone is truly out of range.
				 */
				if (pingFailedCounter >= radioControllerProperties.getPingFailCounter()) {
					LOGGER.info("Ping failed counter >= {}, smartPhone truly out of range. \n Start scanning iPhone.", radioControllerProperties.getPingFailCounter());
					_restartIPhoneScannerTask.cancel();
					startScanning(0);
				}
			}

			@Override
			public void iPhoneDetected() {
				// reset counter
				pingFailedCounter = 0;
				LOGGER.debug(
					"iPhone detected: reset ping failed counter");
				// nothing to do
			}

			@Override
			public String toString() {
				return "restart iPhone scanner task";
			}
		});

		long scanForMobileDeviceEachMillis = TimeUnit.SECONDS.toMillis(radioControllerProperties.getScanRateInSec());
		_scanIPhoneTimer.schedule(_restartIPhoneScannerTask, 0, scanForMobileDeviceEachMillis);
	}

	protected boolean isTimeToPlayMusic() {
		boolean timeToPlay = false;
		int currentHour = LocalTime.now(ZoneId.of("Europe/Berlin")).getHour();
		// don't play at night (had too many sleepless nights)
		int startTime = radioControllerProperties.getStartTimeToPlayMusic();
		int endTime = radioControllerProperties.getEndTimeToPlayMusic();
		if (currentHour >= startTime && currentHour < endTime) {
			timeToPlay = true;
		}
		LOGGER.debug("Time to play music: {} -> (current hour of day: {} >= {} && {} <= {})", timeToPlay, currentHour, startTime, currentHour, endTime);
		return timeToPlay;
	}
}
