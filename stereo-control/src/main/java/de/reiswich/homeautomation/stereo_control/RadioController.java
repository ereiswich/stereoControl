package de.reiswich.homeautomation.stereo_control;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.reiswich.homeautomation.stereo_control.light.LightSwitch;
import de.reiswich.homeautomation.stereo_control.scanning.DetectIPhoneTask;
import de.reiswich.homeautomation.stereo_control.scanning.IPhoneObserver;
import de.reiswich.homeautomation.stereo_control.stereo.AVRActiveSource;
import de.reiswich.homeautomation.stereo_control.stereo.IStopPlayingRadioObserver;
import de.reiswich.homeautomation.stereo_control.stereo.MPCRadioPlayer;
import de.reiswich.homeautomation.stereo_control.stereo.StopRadioPlayingTask;

public class RadioController implements IPhoneObserver {
	private Logger logger = LoggerFactory.getLogger(RadioController.class.getName());

	private Timer _scanIPhoneTimer;
	private final int SCAN_RATE = 10 * 1000; // each 10 seconds

	private DetectIPhoneTask _scanIPhoneTask;

	private DetectIPhoneTask _restartIPhoneScannerTask;

	private Properties _mobileDevices;

	private LightSwitch _lightSwitch;

	public RadioController(Properties props, LightSwitch lightSwitch) {
		_mobileDevices = props;
		_lightSwitch = lightSwitch;
	}

	public void init() {
		startScanning(0);
	}

	@Override
	public void iPhoneDetected() {
		logger.info("\n \t >>> iPhone found. Check if it's time to play music.");
		if (isTimeToPlayMusic()) {
			logger.info("Time to play music = true. Stop scanning and start playing radio");
			stopScanning();
			aquireActiveSource();
			playRadio();
			initStopPlayingTask();
			switchOnLights();
		} else {
			logger.info("Time to play music = false. Restart scanning.");
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

	private void switchOnLights() {
		this._lightSwitch.switchOnLights();
	}

	@Override
	public void iPhoneOffline() {
		// logger.debug("iPhone connection lost.");
	}

	private void startScanning(long delay) {
		logger.info("Initializing scanning tasks");
		_scanIPhoneTask = new DetectIPhoneTask(_mobileDevices);
		_scanIPhoneTask.addIPhoneObserver(this);

		_scanIPhoneTimer = new Timer("iPhone Scanner");
		// scan every x-seconds
		_scanIPhoneTimer.schedule(_scanIPhoneTask, delay, SCAN_RATE);
	}

	private void aquireActiveSource() {
		AVRActiveSource avrActiveSource = new AVRActiveSource();
		avrActiveSource.aquireActiveSource();
	}

	private void playRadio() {
		MPCRadioPlayer radioPlayer = new MPCRadioPlayer();
		radioPlayer.playRadio();
	}

	/*
	 * Stoppe radio nach 90 Minuten, damit es nicht die ganze Nacht durchläuft
	 */
	private void initStopPlayingTask() {
		StopRadioPlayingTask stopRadioPlaying = new StopRadioPlayingTask();
		int minutesForRestart = 90;
		_scanIPhoneTimer.schedule(stopRadioPlaying, minutesForRestart * 60 * 1000); // 60 Min.
		logger.info("Stop playing radio timer initialized after " + minutesForRestart + " Min.");

		stopRadioPlaying.addObserver(new IStopPlayingRadioObserver() {
			@Override
			public void radioPlayingStopped() {
				initRestartScanning();
			}
		});
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
		logger.info("Initializing restart iPhone scanner task");
		_restartIPhoneScannerTask = new DetectIPhoneTask(_mobileDevices);
		_restartIPhoneScannerTask.addIPhoneObserver(new IPhoneObserver() {
			int pingFailedCounter = 0;

			@Override
			public void iPhoneOffline() {
				pingFailedCounter++;
				logger.info("iPhone connection lost. Setting ping failed counter to: " + pingFailedCounter);
				/*
				 * Ping may fail. Don't restart iPhone scanner immediately. Failing e.g. ten
				 * times is more unlikely, thus iPhone is truly out of range.
				 */
				if (pingFailedCounter >= 10) {
					logger.info(
							"Ping failed counter >=10. \n Cancel restart iPhone scanner task. \n Start scanning iPhone.");
					_restartIPhoneScannerTask.cancel();
					startScanning(0);
				}
			}

			@Override
			public void iPhoneDetected() {
				// reset counter
				pingFailedCounter = 0;
				logger.debug(
						"iPhone detected: 1. reset ping failed counter \n 2. nothing to do, iPhone is in range and connected");
				// nothing to do
			}

			@Override
			public String toString() {
				return "restart iPhone scanner task";
			}
		});
		// each 5 Min.
		int scanForMobileDeviceEachMinutes = 5;
		_scanIPhoneTimer.schedule(_restartIPhoneScannerTask, 0, scanForMobileDeviceEachMinutes * 60 * 1000);
	}

	protected boolean isTimeToPlayMusic() {
		boolean timeToPlay = false;
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		// 24 hour clock
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		// don't play at night
		if ( hour >= 11 && hour < 22) {
			timeToPlay = true;
		}
		logger.debug("Time to play music: " + timeToPlay + "\t -> (hour >= 11 && hour < 22)");
		return timeToPlay;
	}
}
