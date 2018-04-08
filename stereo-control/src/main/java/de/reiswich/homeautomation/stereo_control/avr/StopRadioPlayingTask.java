package de.reiswich.homeautomation.stereo_control.avr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stops playing radio after x minutes. Otherwise the radio player will never
 * end and has to be stopped manually.
 * 
 * @author reiswich
 * 
 */
public class StopRadioPlayingTask extends TimerTask {

	private Logger logger = LoggerFactory.getLogger(StopRadioPlayingTask.class);

	private List<IStopPlayingRadioObserver> _observer = new ArrayList<IStopPlayingRadioObserver>();

	@Override
	public void run() {
		try {
			Process process = Runtime.getRuntime().exec("mpc stop");
			process.waitFor();
			logger.info("MPC stop " + " - command sent to AVR");
			informObserver();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	private void informObserver() {
		for (IStopPlayingRadioObserver observer : _observer) {
			observer.radioPlayingStopped();
		}
	}

	public void addObserver(IStopPlayingRadioObserver observer) {
		_observer.add(observer);
	}

	protected void removeObserver(IStopPlayingRadioObserver observer) {
		_observer.remove(observer);
	}

}
