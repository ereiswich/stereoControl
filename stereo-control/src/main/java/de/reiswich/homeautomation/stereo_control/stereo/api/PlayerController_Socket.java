package de.reiswich.homeautomation.stereo_control.stereo.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public PlayerController_Socket(String heosIp, int heosPort) {
		this.heosIp = heosIp;
		this.heosPort = heosPort;
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

	public void readHeosPlayer(){

		String cmd = "heos://player/get_players\\r\\n";

		try (
			Socket socket = new Socket(heosIp, heosPort);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		) {
			out.print(cmd); // Befehl senden
			logger.debug("Befehl gesendet: " + cmd.trim());

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String response;
			while ((response = in.readLine()) != null) {
				logger.debug("Antwort von Heos: " + response);
			}

		} catch (UnknownHostException e) {
			logger.error("Unbekannter Host: " + heosIp);
		} catch (IOException e) {
			logger.error("Fehler bei der Verbindung: " + e.getMessage());
		}
	}

}
