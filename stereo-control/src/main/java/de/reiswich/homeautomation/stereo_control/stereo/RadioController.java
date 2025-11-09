package de.reiswich.homeautomation.stereo_control.stereo;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.reiswich.homeautomation.stereo_control.scanning.DetectIPhoneTask;
import de.reiswich.homeautomation.stereo_control.scanning.IPhoneObserver;
import de.reiswich.homeautomation.stereo_control.stereo.api.IPlayerController;
import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosCommandResponse;

public class RadioController implements IPhoneObserver {
	private Logger LOGGER = LoggerFactory.getLogger(RadioController.class.getName());

	private Timer _scanIPhoneTimer;
	private DetectIPhoneTask _scanIPhoneTask;
	private DetectIPhoneTask _restartIPhoneScannerTask;
	private Properties _mobileDevices;
	private final IPlayerController playerController;
	private final RadioControllerProperties radioControllerProperties;

	public RadioController(Properties props,
		IPlayerController playerController,
		RadioControllerProperties radioControllerProperties) {
		_mobileDevices = props;
		this.playerController = playerController;
		this.radioControllerProperties = radioControllerProperties;
		LOGGER.debug("RadioControllerproperties:  {}", radioControllerProperties);
	}

	public void init() {
		startScanning(0);
	}

	@Override
	public void iPhoneDetected() {
		LOGGER.info("\n \t >>> iPhone found. Check if it's time to play music.");
		if (isTimeToPlayMusic()) {
			LOGGER.info("Time to play music = true. Stop scanning and start playing radio");
			stopScanning();
			startRadioPlayer();
			initStopPlayingTask();

		} else {
			LOGGER.info("Time to play music = false. Restart scanning.");
			/*
			 * Arrived home too late. restart scanning task, otherwise the music will start
			 * to play as soon as: isTimeToPlayMusic() returns true, although you are at
			 * home and playing music is not necessary.
			 */
			stopScanning();
			initRestartScanning();
		}
	}

	private void stopScanning() {
		// stop scanning
		_scanIPhoneTask.cancel();
		// remove scheduled "stop playing tasks" etc.
		_scanIPhoneTimer.purge();
	}

	@Override
	public void iPhoneOffline() {
		// logger.debug("iPhone connection lost.");
	}

	private void startScanning(long delay) {
		LOGGER.info("Initializing scanning tasks");
		_scanIPhoneTask = new DetectIPhoneTask(_mobileDevices);
		_scanIPhoneTask.addIPhoneObserver(this);

		_scanIPhoneTimer = new Timer("iPhone Scanner");
		// scan every x-seconds
		long scanRateInMillis = TimeUnit.SECONDS.toMillis(radioControllerProperties.getScanRateInSec());
		_scanIPhoneTimer.schedule(_scanIPhoneTask, delay, scanRateInMillis);
	}

	private void startRadioPlayer() {
		LOGGER.debug("startRadioPlayer with HEOS-API");

		HeosCommandResponse heosCommandResponse = playerController.playRadio(radioControllerProperties.getPlayerPid());
		LOGGER.debug("playRadio command response: {}", heosCommandResponse);
	}

	/*
	 * Stoppe radio nach 90 Minuten, damit es nicht die ganze Nacht durchläuft
	 */
	private void initStopPlayingTask() {
		StopRadioPlayingTask stopRadioPlaying = new StopRadioPlayingTask(playerController, radioControllerProperties.getPlayerPid());

		stopRadioPlaying.addObserver(new IStopPlayingRadioObserver() {
			@Override
			public void radioPlayingStopped() {
				initRestartScanning();
			}
		});

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
		LOGGER.info("Initializing restart iPhone scanner task");
		_restartIPhoneScannerTask = new DetectIPhoneTask(_mobileDevices);
		_restartIPhoneScannerTask.addIPhoneObserver(new IPhoneObserver() {
			int pingFailedCounter = 0;

			@Override
			public void iPhoneOffline() {
				pingFailedCounter++;
				LOGGER.info("iPhone connection lost. Setting ping failed counter to: " + pingFailedCounter);
				/*
				 * Ping may fail. Don't restart iPhone scanner immediately. Failing e.g. ten
				 * times is more unlikely, thus iPhone is truly out of range.
				 */
				if (pingFailedCounter >= radioControllerProperties.getPingFailCounter()) {
					LOGGER.info(
						"Ping failed counter >= " + radioControllerProperties.getPingFailCounter() + ". \n Cancel restart iPhone scanner task. \n Start scanning iPhone.");
					_restartIPhoneScannerTask.cancel();
					startScanning(0);
				}
			}

			@Override
			public void iPhoneDetected() {
				// reset counter
				pingFailedCounter = 0;
				LOGGER.debug(
					"iPhone detected: 1. reset ping failed counter \n 2. nothing to do, iPhone is in range and connected");
				// nothing to do
			}

			@Override
			public String toString() {
				return "restart iPhone scanner task";
			}
		});

		long scanForMobileDeviceEachMillis = TimeUnit.MINUTES.toMillis(radioControllerProperties.getScanForDevicesInMinutes());
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
