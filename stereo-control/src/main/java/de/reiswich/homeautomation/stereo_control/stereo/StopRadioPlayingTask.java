package de.reiswich.homeautomation.stereo_control.stereo;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.reiswich.homeautomation.stereo_control.stereo.api.IPlayerController;
import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosPlayerResponse;

/**
 * Stops playing radio after x minutes. Otherwise the radio player will never
 * end and has to be stopped manually.
 * 
 * @author reiswich
 * 
 */
public class StopRadioPlayingTask extends TimerTask {

	private final IPlayerController playerController;
	private Logger LOGGER = LoggerFactory.getLogger(StopRadioPlayingTask.class);
	private final int MAX_ATTEMPTS = 3;
	private List<IStopPlayingRadioObserver> _observer = new ArrayList<IStopPlayingRadioObserver>();

	public StopRadioPlayingTask(IPlayerController playerController) {
		this.playerController = playerController;
	}

	@Override
	public void run() {
		LOGGER.info("Trying to stop music player");

		for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
			try {
				HeosPlayerResponse playerResponse = playerController.readHeosPlayer();
				if(playerResponse != null || !playerResponse.getPayload().isEmpty()){
					playerController.stopRadioPlayer(playerResponse.getPayload().get(0).getPid());
				}
				break;
			} catch (Exception e) {
				LOGGER.error("Failed to stop radio player on attempt {}/{}: {}", attempt, MAX_ATTEMPTS, e.getMessage());
				if (attempt == MAX_ATTEMPTS) {
					LOGGER.error("All attempts to stop radio player failed");
				}
			}
		}

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
