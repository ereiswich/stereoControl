package de.reiswich.homeautomation.stereo_control.stereo.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PlayerController_SocketTest {

	@Test
	public void readHeosPlayerTest(){
		// arrange
		PlayerController_Socket playerController = new PlayerController_Socket("192.168.178.127", 1255);

		// act
		playerController.readHeosPlayer();
	}
}