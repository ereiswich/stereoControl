package de.reiswich.homeautomation.stereo_control.stereo.api;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

import org.slf4j.Logger;

/**
 * https://assets.denon.com/documentmaster/uk/avr1713_avr1613_protocol_v860.pdf
 */
public class DenonAvrController_Telnet extends AbstractTelnetController {

	private static final Logger LOGGER = getLogger(DenonAvrController_Telnet.class);

	public DenonAvrController_Telnet(String denonIp, int denonPort) {
		super(denonIp, denonPort);
	}

	public String turnOffAvr() {
		String command = "PWSTANDBY";
		LOGGER.debug("Shutting off AVR Receiver");

		try {
			String result = sendCommand(command);
			// Denon schickt normalerweise als result den Command als String zurück
			if (result != null && !result.equals(command)) {
				// power on fehlgeschlagen, pribier noch einmal
				result = sendCommand(command);
			}
			return result;

		} catch (IOException e) {
			LOGGER.error("Error shutting off Denon AVR: {}", e.getMessage(), e);
			return null;
		}
	}

	public String turnOnAvr() {
		String command = "PWON";
		LOGGER.debug("Shutting on AVR Receiver");
		try {
			String result = sendCommand(command);
			// Denon schickt normalerweise als result den Command als String zurück
			if (result != null && !result.equals(command)) {
				// power on fehlgeschlagen, pribier noch einmal
				result = sendCommand(command);
			}
			return result;

		} catch (IOException e) {
			LOGGER.error("Error shutting on Denon AVR: {}", e.getMessage(), e);
			return null;
		}
	}
}
