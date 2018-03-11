package de.reiswich.homeautomation.plantwatering;

public class StartUp {

	public static void main(String[] args) {
		MoistureSensor sensor = new MoistureSensor();
		sensor.readSensor();
	}
}
