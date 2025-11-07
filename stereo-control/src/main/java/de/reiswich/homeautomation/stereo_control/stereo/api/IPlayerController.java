package de.reiswich.homeautomation.stereo_control.stereo.api;

import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosCommandResponse;
import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosPlayerResponse;

public interface IPlayerController {
	HeosPlayerResponse readHeosPlayer();

	HeosCommandResponse playRadio(long playerId);

	HeosCommandResponse stopRadioPlayer(long playerId);
}
