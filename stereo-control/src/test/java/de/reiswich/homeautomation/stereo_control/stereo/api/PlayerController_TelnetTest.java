package de.reiswich.homeautomation.stereo_control.stereo.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosCommandResponse;
import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosPlayerResponse;

@SpringBootTest
class PlayerController_TelnetTest {

	private PlayerController_Telnet playerController;
	private int playerPid = 860255418;

	@BeforeEach
	public void setUp() {
		playerController = new PlayerController_Telnet("192.168.178.127", 1255);
	}

	@AfterEach
	public void tearDown() {
		// Verbindung nach jedem Test sauber schließen
		if (playerController != null) {
			playerController.close();
		}
	}

	@Test
	public void playRadioTest() {
		// arrange
		playerController = new PlayerController_Telnet("192.168.178.127", 1255);

		// act
		HeosCommandResponse heosCommandResponse = playerController.playRadio(playerPid);

		// assert
		assertNotNull(heosCommandResponse);
		assertEquals("success", heosCommandResponse.getHeos().getResult());
	}

	@Test
	public void stopRadioTest() {
		// arrange
		playerController = new PlayerController_Telnet("192.168.178.127", 1255);

		// act
		HeosCommandResponse heosCommandResponse = playerController.stopRadio(playerPid);

		// assert
		assertNotNull(heosCommandResponse);
		assertEquals("success", heosCommandResponse.getHeos().getResult());
	}

	@Test
	void testStaleConnection_ServerClosesConnection() throws Exception {
		// arrange
		HeosPlayerResponse firstResponse = playerController.readHeosPlayer();
		assertNotNull(firstResponse);
		assertEquals("success", firstResponse.getHeos().getResult());

		// act
		playerController.disconnect();

		// Wait kurz, damit die Verbindung wirklich geschlossen wird
		Thread.sleep(100);

		// assert
		HeosPlayerResponse secondResponse = playerController.readHeosPlayer();
		assertNotNull(secondResponse, "Controller should reconnect after stale connection");
		assertEquals("success", secondResponse.getHeos().getResult());
	}
}
