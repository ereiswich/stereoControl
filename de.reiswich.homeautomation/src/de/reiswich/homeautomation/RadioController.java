package de.reiswich.homeautomation;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

public class RadioController implements CommandProvider, IPhoneObserver {
	private Logger logger = Logger.getLogger(RadioController.class.getName());

	private Timer _scanIPhoneTimer;
	private final int SCAN_RATE = 10 * 1000; // each 10 seconds

	private DetectIPhoneTask _scanIPhoneTask;

	private DetectIPhoneTask _restartIPhoneScannerTask;

	protected void init() {
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
		SunsetService sunsetService = new SunsetService();
		LightSwitch lightSwitch = new LightSwitch();
		lightSwitch.bindSunsetService(sunsetService);
		lightSwitch.switchOnLights();
	}

	@Override
	public void iPhoneOffline() {
		// logger.debug("iPhone connection lost.");
	}

	private void startScanning(long delay) {
		logger.info("Initializing scanning tasks");
		_scanIPhoneTask = new DetectIPhoneTask();
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
		_scanIPhoneTimer.schedule(stopRadioPlaying, 60 * 60 * 1000); // 60 Min.
		logger.info("Stop playing radio timer initialized after 60 Min.");

		stopRadioPlaying.addObserver(new IStopPlayingRadioObserver() {
			@Override
			public void radioPlayingStopped() {
				initRestartScanning();
			}
		});
	}

	@Override
	public String getHelp() {
		return "\trestart <delay in minutes> - restarts timer and iPhone scanner.";
	}

	/**
	 * Restarts the Time tasks that are responsible to scan the iPhone
	 */
	public void _restart(CommandInterpreter ci) {
		logger.debug("Restarting Timer task for iPhone scan");
		int delayInMinutes = 0;
		if (ci != null) {
			String delayStringInMinutes = ci.nextArgument();
			try {
				delayInMinutes = Integer.parseInt(delayStringInMinutes);
				logger.debug("Delaying scanning:" + delayInMinutes + " minutes");
			} catch (NumberFormatException e) {
				logger.error("Unable to parse Number for Delay: " + delayStringInMinutes);
			}
		}
		startScanning(delayInMinutes * 60 * 1000);
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
		_restartIPhoneScannerTask = new DetectIPhoneTask();
		_restartIPhoneScannerTask.addIPhoneObserver(new IPhoneObserver() {
			int pingFailedCounter = 0;

			@Override
			public void iPhoneOffline() {
				pingFailedCounter++;
				logger.info("iPhone connection lost. Setting ping failed counter to: " + pingFailedCounter);
				/*
				 * Ping may fail. Don't restart iPhone scanner immediately. Failing e.g. ten times
				 * is more unlikely, thus iPhone is truly out of range.
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
		_scanIPhoneTimer.schedule(_restartIPhoneScannerTask, 0, 5 * 60 * 1000);
	}

	protected boolean isTimeToPlayMusic() {
		boolean timeToPlay = false;
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		// 24 hour clock
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		// don't play at night
		if (24 >= hour && hour >= 10) {
			timeToPlay = true;
		}
		logger.debug("Time to play music: " + timeToPlay + "\t -> (24 >= hour && hour >= 10)");
		return timeToPlay;
	}
}
