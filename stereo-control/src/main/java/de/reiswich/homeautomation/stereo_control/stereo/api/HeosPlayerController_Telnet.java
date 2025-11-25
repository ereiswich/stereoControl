package de.reiswich.homeautomation.stereo_control.stereo.api;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosCommandResponse;
import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosPlayerResponse;

/**
 * HEOS API Documentation:
 * https://rn.dmglobal.com/usmodel/HEOS_CLI_ProtocolSpecification-Version-1.17.pdf
 */
public class HeosPlayerController_Telnet extends AbstractTelnetController implements IPlayerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(HeosPlayerController_Telnet.class);

	private final ObjectMapper objectMapper;

	public HeosPlayerController_Telnet(String heosIp, int heosPort) {
		super(heosIp, heosPort);
		this.objectMapper = new ObjectMapper();
		this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public HeosCommandResponse playRadio(long playerId) {
		LOGGER.debug("Playing radio with playerId: {}", playerId);
		int beatsRadioStationId = 1;
		String command = String.format("heos://browse/play_preset?pid=%d&preset=%d",
			playerId, beatsRadioStationId);

		LOGGER.debug("Executing HEOS command: {}", command);

		try {
			String response = sendCommand(command);
			return objectMapper.readValue(response, HeosCommandResponse.class);

		} catch (IOException e) {
			LOGGER.error("Error executing HEOS command: {}", e.getMessage(), e);
			return null;
		}
	}

	public HeosCommandResponse setVolume(long playerId, int volume) {
		LOGGER.debug("Set radio volume with playerId: {} and volume: {}", playerId, volume);
		String command = String.format("heos://player/set_volume?pid=%d&level=%d",
			playerId, volume);

		LOGGER.debug("Executing HEOS command: {}", command);

		try {
			String response = sendCommand(command);
			return objectMapper.readValue(response, HeosCommandResponse.class);

		} catch (IOException e) {
			LOGGER.error("Error executing HEOS command: {}", e.getMessage(), e);
			return null;
		}
	}

	/**
	 * just for tests
	 *
	 * @return
	 */
	public HeosPlayerResponse readHeosPlayer() {
		String command = "heos://player/get_players";
		LOGGER.debug("Reading HEOS players");

		try {
			String response = sendCommand(command);
			return objectMapper.readValue(response, HeosPlayerResponse.class);

		} catch (IOException e) {
			LOGGER.error("Error reading HEOS players: {}", e.getMessage(), e);
			return null;
		}
	}

	@Override
	public HeosCommandResponse stopRadio(long playerId) {
		String command = String.format("heos://player/set_play_state?pid=%d&state=stop", playerId);
		LOGGER.debug("Stopping radio player with playerId: {} and command: {}", playerId, command);

		try {
			String response = sendCommand(command);
			return objectMapper.readValue(response, HeosCommandResponse.class);

		} catch (IOException e) {
			LOGGER.error("Error executing HEOS command: {}", e.getMessage(), e);
			return null;
		}
	}

}
