package de.reiswich.homeautomation.stereo_control.stereo;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.reiswich.homeautomation.stereo_control.stereo.api.PlayerController_Socket;

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



	public StopRadioPlayingTask( ) {
		
	}

	@Override
	public void run() {
		logger.info("Trying to stop music player");
		PlayerController_Socket.stopPlayer();
		logger.info("AVR stop player command sent");
		informObserver();
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
