package de.reiswich.homeautomation.stereo_control.stereo.api;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;

/**
 * Kapselt gemeinsame Logik zur Steuerung vom Denon  AVR und HEOS CLI
 */
public abstract class AbstractTelnetController {

	protected final Logger logger = getLogger(getClass());
	private final String ip;
	private final int port;
	private TelnetClient telnetClient;
	private PrintWriter writer;
	private BufferedReader reader;

	private static final int CONNECTION_TIMEOUT = 5000;
	private static final int MAX_RETRIES = 3;
	private static final int RETRY_DELAY_MS = 2000;

	private static final Logger LOGGER = getLogger(AbstractTelnetController.class);

	protected AbstractTelnetController(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	protected synchronized String sendCommand(String command) throws IOException {
		return this.sendCommand(command, new ArrayList<>());
	}

	protected synchronized String sendCommand(String command, List<String> expectedResponse) throws IOException {
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
				} else if (!expectedResponse.isEmpty() && !expectedResponse.contains(response)) {
					LOGGER.debug("Unexpected response string: {}", response);
					throw new IOException("Unexpected response: " + response);
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

	private void connect() throws IOException {
		if (telnetClient != null && telnetClient.isConnected()) {
			return;
		}
		telnetClient = new TelnetClient();
		telnetClient.setConnectTimeout(CONNECTION_TIMEOUT);
		telnetClient.connect(ip, port);

		reader = new BufferedReader(new InputStreamReader(telnetClient.getInputStream(), StandardCharsets.UTF_8));
		writer = new PrintWriter(new OutputStreamWriter(telnetClient.getOutputStream(), StandardCharsets.UTF_8), true);
	}

	// wird in PreDestroy in der {@link Configuration} aufgerufen
	public void disconnect() {
		// Gemeinsame Disconnect-Logik
		try {
			if (telnetClient != null)
				telnetClient.disconnect();
		} catch (IOException e) {
			logger.error("Error disconnecting telnet", e);
		}
	}

	private void waitBeforeRetry() {
		try {
			LOGGER.debug("Waiting {}ms before retry...", RETRY_DELAY_MS);
			Thread.sleep(RETRY_DELAY_MS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
