package de.reiswich.homeautomation.plantwatering;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class MoistureSensor {

	// Device address
	private static final int deviceAddress = 0x48;

	// Temperature Control Register Data
	private static final int controlRegister = 0x41;

	private I2CBus i2cBus;

	private I2CDevice moistureSensor;

	public MoistureSensor() {

	}

	protected void readSensor() {
		try {
			i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);
			moistureSensor = i2cBus.getDevice(deviceAddress);

			int value = moistureSensor.read(controlRegister);
			System.out.println("Moisture value: " + value);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
