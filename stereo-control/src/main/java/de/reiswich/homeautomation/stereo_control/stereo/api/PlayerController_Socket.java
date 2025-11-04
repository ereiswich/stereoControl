package de.reiswich.homeautomation.stereo_control.stereo.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
	private static Logger logger = LoggerFactory.getLogger(PlayerController_Socket.class.getName());
	private final String heosIp;
	private final int heosPort;
	private ObjectMapper objectMapper;

	public PlayerController_Socket(String heosIp, int heosPort) {
		this.heosIp = heosIp;
		this.heosPort = heosPort;
		this.objectMapper = new ObjectMapper();
	}
 
	public static void startPlayer() {
		try {
			Runtime r = Runtime.getRuntime();
			Process p = r
					.exec("python /home/pi/projects/radioplay/StartPlayerCommand.py");
			p.waitFor();

			logger.info("StartPlayerCommand sent to AVR");
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void stopPlayer() {
		try {
			Runtime r = Runtime.getRuntime();
			Process p = r
					.exec("python /home/pi/projects/radioplay/StopPlayerCommand.py");
			p.waitFor();

			logger.info("StopPlayerCommand sent to AVR");
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public HeosPlayerResponse readHeosPlayer(){

		String cmd = "heos://player/get_players";
		HeosPlayerResponse playerResponse = null;
		try {
			Socket socket = new Socket(heosIp, heosPort);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			out.println(cmd); // Befehl senden
			logger.debug("Befehl gesendet: {} mit IP: {} und Port: {} ", cmd.trim(), heosIp, heosPort);

			String response = in.readLine();
			logger.debug("Antwort von Heos: " + response);
			playerResponse = objectMapper.readValue(response, HeosPlayerResponse.class);

			in.close();
			out.close();
			socket.close();
		} catch (UnknownHostException e) {
			logger.error("Unbekannter Host: " + heosIp);
		} catch (IOException e) {
			logger.error("Fehler bei der Verbindung: " + e.getMessage());
		}
		return playerResponse;
	}

	public HeosCommandResponse playRadio(long playerId) {
		logger.debug("play Radio with playerId: " + playerId);
		int beatsRadioStationId =1;
		String cmd = "heos://browse/play_preset?pid="+playerId+"&preset="+beatsRadioStationId;

		HeosCommandResponse heosCommandResponse = null;
		try {
			Socket socket = new Socket(heosIp, heosPort);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			out.println(cmd); // Befehl senden
			logger.debug("Befehl gesendet: {} mit IP: {} und Port: {} ", cmd.trim(), heosIp, heosPort);

			String response = in.readLine();
			 heosCommandResponse = objectMapper.readValue(response, HeosCommandResponse.class);
			logger.debug("Antwort von Heos: " + heosCommandResponse.getHeos().getResult());
			in.close();
			out.close();
			socket.close();
		} catch (UnknownHostException e) {
			logger.error("Unbekannter Host: " + heosIp);
		} catch (IOException e) {
			logger.error("Fehler bei der Verbindung: " + e.getMessage());
		}
		return heosCommandResponse;
	}

	public HeosCommandResponse stopRadioPlayer(long playerId){
		logger.debug("stop Radio with playerId: " + playerId);
		String cmd = "heos://player/set_play_state?pid="+playerId+"&state=stop";
		HeosCommandResponse heosCommandResponse = null;
		try {
			Socket socket = new Socket(heosIp, heosPort);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			out.println(cmd); // Befehl senden
			logger.debug("Befehl gesendet: {} mit IP: {} und Port: {} ", cmd.trim(), heosIp, heosPort);

			String response = in.readLine();
			 heosCommandResponse = objectMapper.readValue(response, HeosCommandResponse.class);
			logger.debug("Antwort von Heos: " + heosCommandResponse.getHeos().getResult());

			in.close();
			out.close();
			socket.close();
		} catch (UnknownHostException e) {
			logger.error("Unbekannter Host: " + heosIp);
		} catch (IOException e) {
			logger.error("Fehler bei der Verbindung: " + e.getMessage());
		}
		return heosCommandResponse;
	}

}
