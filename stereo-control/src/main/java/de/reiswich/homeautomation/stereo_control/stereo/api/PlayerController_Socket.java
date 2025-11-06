package de.reiswich.homeautomation.stereo_control.stereo.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosCommandResponse;
import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosPlayerResponse;

/**
 * Send a signal to AV-Receiver to set the active source to RPi. That means, the
 * AV-Receiver will play the sound the RPi produces.
 *
 * @author reiswich
 *
 */
public class PlayerController_Socket {
	private final static Logger LOGGER = LoggerFactory.getLogger(PlayerController_Socket.class.getName());
	private final String heosIp;
	private final int heosPort;
	private final ObjectMapper objectMapper;
	private static final int MAX_RETRIES = 3;
	private static final long RETRY_DELAY_MS = 1000;
	private static final int CONNECTION_TIMEOUT_MS = 5000;

	public PlayerController_Socket(String heosIp, int heosPort) {
		this.heosIp = heosIp;
		this.heosPort = heosPort;
		this.objectMapper = new ObjectMapper();
	}

	public HeosPlayerResponse readHeosPlayer() {

		String cmd = "heos://player/get_players";
		HeosPlayerResponse playerResponse = null;

		for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {

			try (Socket socket = new Socket()){
				socket.connect(new InetSocketAddress(heosIp, heosPort), CONNECTION_TIMEOUT_MS);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				out.println(cmd); // Befehl senden
				LOGGER.debug("Befehl gesendet: {} mit IP: {} und Port: {} ", cmd.trim(), heosIp, heosPort);

				String response = in.readLine();
				LOGGER.debug("Antwort von Heos: " + response);
				playerResponse = objectMapper.readValue(response, HeosPlayerResponse.class);

				// Erfolgreicher Versuch - Schleife verlassen
				LOGGER.debug("HEOS-Verbindung erfolgreich beim Versuch {}/{}", attempt, MAX_RETRIES);
				break;
			} catch (UnknownHostException e) {
				LOGGER.error("Unbekannter Host: " + heosIp);
			} catch (IOException e) {
				LOGGER.error("Fehler bei der Verbindung: " + e.getMessage());
				if (attempt < MAX_RETRIES) {
					waitBeforeRetry();
				} else {
					LOGGER.error("Alle HEOS-Verbindungsversuche fehlgeschlagen");
				}
			}
		}
		return playerResponse;
	}

	public HeosCommandResponse playRadio(long playerId) {
		LOGGER.debug("play Radio with playerId: " + playerId);
		int beatsRadioStationId = 1;
		String cmd = "heos://browse/play_preset?pid=" + playerId + "&preset=" + beatsRadioStationId;

		return executeRadioPlayerCommand(playerId, cmd);
	}

	public HeosCommandResponse stopRadioPlayer(long playerId) {
		LOGGER.debug("stop Radio with playerId: " + playerId);
		String cmd = "heos://player/set_play_state?pid=" + playerId + "&state=stop";
		return executeRadioPlayerCommand(playerId, cmd);
	}

	protected HeosCommandResponse executeRadioPlayerCommand(long playerId, String command) {
		LOGGER.debug("execute Radio Player Command with playerId: {} and cmd: {}", playerId, command);
		HeosCommandResponse heosCommandResponse = null;
		for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {

			try(Socket socket = new Socket()) {
				socket.connect(new InetSocketAddress(heosIp, heosPort), CONNECTION_TIMEOUT_MS);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				out.println(command); // Befehl senden
				LOGGER.debug("Befehl gesendet: {} mit IP: {} und Port: {} ", command.trim(), heosIp, heosPort);

				String response = in.readLine();
				heosCommandResponse = objectMapper.readValue(response, HeosCommandResponse.class);
				LOGGER.debug("Antwort von Heos: " + heosCommandResponse.getHeos().getResult());

				LOGGER.debug("HEOS-Command erfolgreich beim Versuch {}/{}", attempt, MAX_RETRIES);
				break;
			} catch (UnknownHostException e) {
				LOGGER.error("Unbekannter Host: " + heosIp);
			} catch (IOException e) {
				LOGGER.error("Fehler beim Aufbau der HEOS-Verbindung: " + e.getMessage());
				if (attempt < MAX_RETRIES) {
					waitBeforeRetry();
				} else {
					LOGGER.error("Alle Verbindungsversuche fehlgeschlagen");
				}
			}
		}
		return heosCommandResponse;
	}

	/**
	 * Waits before retrying a failed connection attempt.
	 * Handles interruption gracefully.
	 */
	private void waitBeforeRetry() {
		try {
			LOGGER.info("Waiting {} ms before retry...", RETRY_DELAY_MS);
			Thread.sleep(RETRY_DELAY_MS);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			LOGGER.error("Retry interrupted, aborting further attempts");
		}
	}
}
