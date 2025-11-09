package de.reiswich.homeautomation.stereo_control.stereo.api.dto;

public class HeosHeader {
	private String command;
	private String result;
	private String message;

	public HeosHeader() {
	}

	public HeosHeader(String command, String result, String message) {
		this.command = command;
		this.result = result;
		this.message = message;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override public String toString() {
		return "HeosHeader{" +
			"command='" + command + '\'' +
			", result='" + result + '\'' +
			", message='" + message + '\'' +
			'}';
	}
}