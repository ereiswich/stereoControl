package de.reiswich.homeautomation.stereo_control.stereo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "radiocontrol")
public class RadioControllerProperties {
	private long restartScanTaskAfterMinutes;
	private int pingFailCounter;
	private int scanForDevicesInMinutes;
	private int startTimeToPlayMusic;
	private int endTimeToPlayMusic;
	private int scanRateInSec;
	private int playerPid;

	public long getRestartAfterMinutes() {
		return restartScanTaskAfterMinutes;
	}

	public void setRestartAfterMinutes(long restartAfterMinutes) {
		this.restartScanTaskAfterMinutes = restartAfterMinutes;
	}

	public int getPingFailCounter() {
		return pingFailCounter;
	}

	public void setPingFailCounter(int pingFailCounter) {
		this.pingFailCounter = pingFailCounter;
	}

	public int getScanForDevicesInMinutes() {
		return scanForDevicesInMinutes;
	}

	public void setScanForDevicesInMinutes(int scanForDevicesInMinutes) {
		this.scanForDevicesInMinutes = scanForDevicesInMinutes;
	}

	public int getStartTimeToPlayMusic() {
		return startTimeToPlayMusic;
	}

	public void setStartTimeToPlayMusic(int startTimeToPlayMusic) {
		this.startTimeToPlayMusic = startTimeToPlayMusic;
	}

	public int getEndTimeToPlayMusic() {
		return endTimeToPlayMusic;
	}

	public void setEndTimeToPlayMusic(int endTimeToPlayMusic) {
		this.endTimeToPlayMusic = endTimeToPlayMusic;
	}

	public int getScanRateInSec() {
		return scanRateInSec;
	}

	public void setScanRateInSec(int scanRateInSec) {
		this.scanRateInSec = scanRateInSec;
	}

	public int getPlayerPid() {
		return playerPid;
	}

	public void setPlayerPid(int playerPid) {
		this.playerPid = playerPid;
	}

	@Override public String toString() {
		return "RadioControllerProperties{" +
			"restartAfterMinutes=" + restartScanTaskAfterMinutes +
			", pingFailCounter=" + pingFailCounter +
			", scanForDevicesInMinutes=" + scanForDevicesInMinutes +
			", startTimeToPlayMusic=" + startTimeToPlayMusic +
			", endTimeToPlayMusic=" + endTimeToPlayMusic +
			", scanRateInSec=" + scanRateInSec +
			", playerPid=" + playerPid +
			'}';
	}
}
