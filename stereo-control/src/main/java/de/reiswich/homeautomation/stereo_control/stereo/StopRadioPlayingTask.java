package de.reiswich.homeautomation.stereo_control.stereo;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.reiswich.homeautomation.stereo_control.stereo.api.DenonAvrController_Telnet;
import de.reiswich.homeautomation.stereo_control.stereo.api.IPlayerController;
import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosCommandResponse;

/**
 * Stops playing radio after x minutes. Otherwise the radio player will never
 * end and has to be stopped manually.
 *
 * @author reiswich
 *
 */
public class StopRadioPlayingTask extends TimerTask {

	private final IPlayerController playerController;
	private final DenonAvrController_Telnet denonAvrController;
	private final int playerPid;
	private Logger LOGGER = LoggerFactory.getLogger(StopRadioPlayingTask.class);
	private final int MAX_ATTEMPTS = 3;
	private List<IStopPlayingRadioObserver> _observer = new ArrayList<IStopPlayingRadioObserver>();

	public StopRadioPlayingTask(IPlayerController playerController, DenonAvrController_Telnet denonAvrController, int playerPid) {
		this.playerController = playerController;
		this.denonAvrController = denonAvrController;
		this.playerPid = playerPid;
	}

	@Override
	public void run() {

		for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
			try {
				LOGGER.debug("Trying to turn off Denon AVR music player");
				String avrResponse = denonAvrController.turnOffAvr();
				LOGGER.debug("Turn Off command AVR response: {}", avrResponse);

				LOGGER.debug("Trying to switch off music player");
				HeosCommandResponse heosCommandResponse = playerController.stopRadio(playerPid);
				LOGGER.debug("stopRadio command response: {}", heosCommandResponse);

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
