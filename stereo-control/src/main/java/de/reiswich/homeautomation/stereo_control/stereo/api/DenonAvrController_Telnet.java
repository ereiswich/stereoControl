package de.reiswich.homeautomation.stereo_control.stereo.api;

import static org.slf4j.LoggerFactory.getLogger;

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

public class DenonAvrController_Telnet {

	private final String denonIp;
	private final int denonPort;
	private static final int CONNECTION_TIMEOUT = 5000;
	private static final int MAX_RETRIES = 3;
	private static final int RETRY_DELAY_MS = 2000;

	private TelnetClient telnetClient;
	private BufferedReader reader;
	private PrintWriter writer;
	private static final Logger LOGGER = getLogger(DenonAvrController_Telnet.class);

	public DenonAvrController_Telnet(String denonIp, int denonPort) {
		this.denonIp = denonIp;
		this.denonPort = denonPort;
	}

	private void connect() throws IOException {
		if (telnetClient != null && telnetClient.isConnected()) {
			return;
		}

		telnetClient = new TelnetClient();
		telnetClient.setConnectTimeout(CONNECTION_TIMEOUT);

		LOGGER.debug("Connecting to Denon AVR via Telnet: {}:{}", denonIp, denonPort);
		telnetClient.connect(denonIp, denonPort);

		InputStream inputStream = telnetClient.getInputStream();
		OutputStream outputStream = telnetClient.getOutputStream();

		reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

		LOGGER.info("Successfully connected to Denon AVR via Telnet");
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
				LOGGER.debug("Disconnected from Denon AVR");
			}
		} catch (IOException e) {
			LOGGER.error("Error while disconnecting from Denon AVR", e);
		} finally {
			telnetClient = null;
			reader = null;
			writer = null;
		}
	}

	public String turnOffAvr() {
		String command = "PWSTANDBY";
		LOGGER.debug("Shutting off AVR Receiver");

		try {
			return sendCommand(command);

		} catch (IOException e) {
			LOGGER.error("Error shutting off Denon AVR: {}", e.getMessage(), e);
			return null;
		}
	}

	public String turnOnAvr() {
		String command = "PWON";
		LOGGER.debug("Shutting on AVR Receiver");

		try {
			return sendCommand(command);

		} catch (IOException e) {
			LOGGER.error("Error shutting on Denon AVR: {}", e.getMessage(), e);
			return null;
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
				LOGGER.error("Error sending Denon AVR command (attempt {}/{}): {}", attempt, MAX_RETRIES, e.getMessage());
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

	private void waitBeforeRetry() {
		try {
			LOGGER.debug("Waiting {}ms before retry to connect to AVR...", RETRY_DELAY_MS);
			Thread.sleep(RETRY_DELAY_MS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error("Retry wait interrupted", e);
		}
	}
}
