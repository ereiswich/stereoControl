package de.reiswich.homeautomation.stereo_control.light;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightSwitch {

	Logger logger = LoggerFactory.getLogger(LightSwitch.class.getName());
	private SunsetService sunsetService;
	
	public LightSwitch(SunsetService sunsetService) {
		this.sunsetService = sunsetService;
	}

	public void switchOnLights() {
		if (sunsetService.isTimeToSwitchOnLights()) {
			logger.debug("Time to swith on lights = true");
			try {
				Runtime runtime = Runtime.getRuntime();
				runtime.exec("tdtool --on 1");
				logger.info("switching on light: 1");

				runtime.exec("tdtool --on 2");
				logger.info("switching on light: 2");

				runtime.exec("tdtool --on 3");
				logger.info("switching on light: 3");
			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		} else {
			logger.debug("Time to swith on lights = false");
		}
	}
}
