package de.reiswich.homeautomation.stereo_control.stereo.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DenonAvrController_TelnetTest {

	private DenonAvrController_Telnet denonAvrController;

	@BeforeEach
	public void setUp() {
		denonAvrController = new DenonAvrController_Telnet("192.168.178.127", 23);
	}

	@Test
	void turnOnAvr_shouldSendCommandPWON() {

		// Act
		String response = denonAvrController.turnOnAvr();

		// assert
		assertThat(response).isNotEmpty();
	}

	@Test
	void turnOffAvr_shouldSendCommandPWSTANDBY() {

		// Act
		String response = denonAvrController.turnOffAvr();

		// assert
		Assertions.assertThat(response).isNotEmpty();
	}

}
