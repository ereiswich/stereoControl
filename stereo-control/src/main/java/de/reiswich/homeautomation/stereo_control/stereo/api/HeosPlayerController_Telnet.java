package de.reiswich.homeautomation.stereo_control.stereo.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.net.telnet.TelnetClient;
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
public class HeosPlayerController_Telnet implements IPlayerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(HeosPlayerController_Telnet.class);
	private static final int CONNECTION_TIMEOUT = 5000;
	private static final int MAX_RETRIES = 3;
	private static final int RETRY_DELAY_MS = 2000;

	private final String heosIp;
	private final int heosPort;
	private final ObjectMapper objectMapper;

	private TelnetClient telnetClient;
	private BufferedReader reader;
	private PrintWriter writer;

	public HeosPlayerController_Telnet(String heosIp, int heosPort) {
		this.heosIp = heosIp;
		this.heosPort = heosPort;
		this.objectMapper = new ObjectMapper();
		this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private void connect() throws IOException {
		if (telnetClient != null && telnetClient.isConnected()) {
			return;
		}

		telnetClient = new TelnetClient();
		telnetClient.setConnectTimeout(CONNECTION_TIMEOUT);

		LOGGER.debug("Connecting to HEOS via Telnet: {}:{}", heosIp, heosPort);
		telnetClient.connect(heosIp, heosPort);

		InputStream inputStream = telnetClient.getInputStream();
		OutputStream outputStream = telnetClient.getOutputStream();

		reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

		LOGGER.info("Successfully connected to HEOS via Telnet");
	}

	protected void disconnect() {
		try {
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
			if (telnetClient != null && telnetClient.isConnected()) {
				telnetClient.disconnect();
				LOGGER.debug("Disconnected from HEOS");
			}
		} catch (IOException e) {
			LOGGER.error("Error while disconnecting", e);
		} finally {
			telnetClient = null;
			reader = null;
			writer = null;
		}
	}

	private String sendCommand(String command) throws IOException {
		for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
			try {
				connect();

				LOGGER.debug("Sending command (attempt {}/{}): {}", attempt, MAX_RETRIES, command);
				writer.println(command);
				writer.flush(); // Sicherstellen, dass der Befehl wirklich gesendet wird

				String response = reader.readLine();
				LOGGER.debug("Received response: {}", response);
				if (response == null) {
					// Verbindung wurde vom Server geschlossen
					throw new IOException("Connection closed by server (readLine returned null)");
				}

				return response;

			} catch (IOException e) {
				LOGGER.error("Error sending command (attempt {}/{}): {}", attempt, MAX_RETRIES, e.getMessage());
				disconnect(); // Wichtig: Bei Fehler Verbindung trennen, damit beim nächsten Versuch neu verbunden wird

				if (attempt < MAX_RETRIES) {
					waitBeforeRetry();
				} else {
					throw e;
				}
			}
		}

		throw new IOException("All connection attempts failed");
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

	private void waitBeforeRetry() {
		try {
			LOGGER.debug("Waiting {}ms before retry...", RETRY_DELAY_MS);
			Thread.sleep(RETRY_DELAY_MS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error("Retry wait interrupted", e);
		}
	}

	/**
	 * Schließt die Telnet-Verbindung. Sollte beim Herunterfahren aufgerufen werden.
	 */
	public void close() {
		disconnect();
	}
}
