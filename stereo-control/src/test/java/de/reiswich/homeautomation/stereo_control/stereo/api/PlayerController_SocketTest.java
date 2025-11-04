package de.reiswich.homeautomation.stereo_control.stereo.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosCommandResponse;
import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosPlayerResponse;

@SpringBootTest
class PlayerController_SocketTest {

	@Test
	public void readHeosPlayerTest(){
		// arrange
		PlayerController_Socket playerController = new PlayerController_Socket("192.168.178.127", 1255);

		// act
		playerController.readHeosPlayer();
	}

	@Test
	public void playRadioTest(){
		// arrange
		PlayerController_Socket playerController = new PlayerController_Socket("192.168.178.127", 1255);
		HeosPlayerResponse playerResponse =
			playerController.readHeosPlayer();
		// act
		HeosCommandResponse heosCommandResponse = playerController.playRadio(playerResponse.getPayload().get(0).getPid());
		assertEquals("success", heosCommandResponse.getHeos().getResult());
	}

	@Test
	public void stopRadioTest(){
		// arrange
		PlayerController_Socket playerController = new PlayerController_Socket("192.168.178.127", 1255);
		HeosPlayerResponse playerResponse =
			playerController.readHeosPlayer();
		// act
		HeosCommandResponse heosCommandResponse = playerController.stopRadioPlayer(playerResponse.getPayload().get(0).getPid());
		assertEquals("success", heosCommandResponse.getHeos().getResult());
	}
}