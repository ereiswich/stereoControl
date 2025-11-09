package de.reiswich.homeautomation.stereo_control.stereo.api;

import de.reiswich.homeautomation.stereo_control.stereo.api.dto.HeosCommandResponse;

public interface IPlayerController {
	HeosCommandResponse setVolume(long playerId, int volume);

	HeosCommandResponse playRadio(long playerId);

	HeosCommandResponse stopRadio(long playerId);
}
