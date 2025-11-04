package de.reiswich.homeautomation.stereo_control.stereo.api.dto;

public class HeosPlayer {
	private String name;
	private long pid;
	private String model;
	private String version;
	private String ip;
	private String network;
	private int lineout;
	private String serial;

	public HeosPlayer() {
	}

	public HeosPlayer(String name, long pid, String model, String version,
		String ip, String network, int lineout, String serial) {
		this.name = name;
		this.pid = pid;
		this.model = model;
		this.version = version;
		this.ip = ip;
		this.network = network;
		this.lineout = lineout;
		this.serial = serial;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public int getLineout() {
		return lineout;
	}

	public void setLineout(int lineout) {
		this.lineout = lineout;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}
}