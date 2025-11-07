package de.reiswich.homeautomation.stereo_control.stereo.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosCommandResponse;
import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosPlayerResponse;

@SpringBootTest
class PlayerController_TelnetTest {

	private PlayerController_Telnet playerController;

	@AfterEach
	public void tearDown() {
		// Verbindung nach jedem Test sauber schließen
		if (playerController != null) {
			playerController.close();
		}
	}

	@Test
	public void readHeosPlayerTest() {
		// arrange
		playerController = new PlayerController_Telnet("192.168.178.127", 1255);

		// act
		HeosPlayerResponse playerResponse = playerController.readHeosPlayer();

		// assert
		assertNotNull(playerResponse);
		assertNotNull(playerResponse.getPayload());
		assertFalse(playerResponse.getPayload().isEmpty());
	}

	@Test
	public void playRadioTest() {
		// arrange
		playerController = new PlayerController_Telnet("192.168.178.127", 1255);
		HeosPlayerResponse playerResponse = playerController.readHeosPlayer();

		// act
		HeosCommandResponse heosCommandResponse = playerController.playRadio(playerResponse.getPayload().get(0).getPid());

		// assert
		assertNotNull(heosCommandResponse);
		assertEquals("success", heosCommandResponse.getHeos().getResult());
	}

	@Test
	public void stopRadioTest() {
		// arrange
		playerController = new PlayerController_Telnet("192.168.178.127", 1255);
		HeosPlayerResponse playerResponse = playerController.readHeosPlayer();

		// act
		HeosCommandResponse heosCommandResponse = playerController.stopRadioPlayer(playerResponse.getPayload().get(0).getPid());

		// assert
		assertNotNull(heosCommandResponse);
		assertEquals("success", heosCommandResponse.getHeos().getResult());
	}
}
