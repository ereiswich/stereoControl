package de.reiswich.homeautomation.stereo_control.stereo.api.dto;

import java.util.List;

public class HeosPlayerResponse {
	private HeosHeader heos;
	private List<HeosPlayer> payload;

	public HeosPlayerResponse() {
	}

	public HeosPlayerResponse(HeosHeader heos, List<HeosPlayer> payload) {
		this.heos = heos;
		this.payload = payload;
	}

	public HeosHeader getHeos() {
		return heos;
	}

	public void setHeos(HeosHeader heos) {
		this.heos = heos;
	}

	public List<HeosPlayer> getPayload() {
		return payload;
	}

	public void setPayload(List<HeosPlayer> payload) {
		this.payload = payload;
	}
}