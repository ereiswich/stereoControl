package de.reiswich.homeautomation.stereo_control.stereo.api;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
		List<String> expectedResponse = Arrays.asList("Z2OFF", "PWSTANDBY");
		LOGGER.debug("Shutting off AVR Receiver");

		try {
			return sendCommand(command, expectedResponse);

		} catch (IOException e) {
			LOGGER.error("Error shutting off Denon AVR: {}", e.getMessage(), e);
			return null;
		}
	}

	public String turnOnAvr() {
		String command = "PWON";
		List<String> expectedResponse = Arrays.asList("PWON");
		LOGGER.debug("Turning on AVR Receiver");
		try {
			return sendCommand(command, expectedResponse);

		} catch (IOException e) {
			LOGGER.error("Error turning on Denon AVR: {}", e.getMessage(), e);
			return null;
		}
	}
}
