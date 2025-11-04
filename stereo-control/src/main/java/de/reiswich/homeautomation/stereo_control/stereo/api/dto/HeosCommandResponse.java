package de.reiswich.homeautomation.stereo_control.stereo.api.dto;

public class HeosCommandResponse {
	private HeosHeader heos;

	public HeosCommandResponse() {
	}

	public HeosCommandResponse(HeosHeader heos) {
		this.heos = heos;
	}

	public HeosHeader getHeos() {
		return heos;
	}

	public void setHeos(HeosHeader heos) {
		this.heos = heos;
	}
}
