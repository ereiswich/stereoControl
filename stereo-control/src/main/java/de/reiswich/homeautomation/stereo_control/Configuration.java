package de.reiswich.homeautomation.stereo_control;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import de.reiswich.homeautomation.stereo_control.stereo.RadioController;
import de.reiswich.homeautomation.stereo_control.stereo.RadioControllerProperties;
import de.reiswich.homeautomation.stereo_control.stereo.api.DenonAvrController_Telnet;
import de.reiswich.homeautomation.stereo_control.stereo.api.HeosPlayerController_Telnet;
import de.reiswich.homeautomation.stereo_control.stereo.api.IPlayerController;

@org.springframework.context.annotation.Configuration
@PropertySource("classpath:application.properties")
public class Configuration {

	@Value("${denon.ip}")
	private String denonIp;

	@Value("${heos.port}")
	private int heosPort;

	@Value("${denon.port}")
	private int denonPort;

	private Logger LOGGER = LoggerFactory.getLogger(Configuration.class.getName());

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	private HeosPlayerController_Telnet playerControllerTelnet;
	private DenonAvrController_Telnet denonAvrController;

	@Bean
	public RadioController getRadioController() {
		LOGGER.debug("initializing RadioController");
		RadioController radioController = new RadioController(getMobileDevicesProperties(),
			playerControllerTelnet(),
			denonAvrControllerTelnet(),
			getRadioControllerProperties());
		radioController.init();
		LOGGER.debug("RadioController initialized");
		return radioController;
	}

	@Bean
	public RadioControllerProperties getRadioControllerProperties() {
		return new RadioControllerProperties();
	}

	@Bean
	public IPlayerController playerControllerTelnet() {
		if (playerControllerTelnet == null) {
			playerControllerTelnet = new HeosPlayerController_Telnet(denonIp, heosPort);
		}
		return playerControllerTelnet;
	}

	@Bean
	public DenonAvrController_Telnet denonAvrControllerTelnet() {
		if (denonAvrController == null) {
			denonAvrController = new DenonAvrController_Telnet(denonIp, denonPort);
		}
		return denonAvrController;
	}

	@PreDestroy
	public void cleanup() {
		LOGGER.info("Shutting down application - cleaning up resources");

		if (playerControllerTelnet != null) {
			try {
				LOGGER.debug("Closing Telnet connection");
				playerControllerTelnet.close();
				LOGGER.info("Telnet connection closed successfully");
			} catch (Exception e) {
				LOGGER.error("Error while closing Telnet connection", e);
			}
		}

		LOGGER.info("Cleanup completed");
	}

	private Properties getMobileDevicesProperties() {
		LOGGER.debug("Trying to read MobileDevice-Properties");
		Properties movileDevicesProps = new Properties();
		try {
			/*
			 * um an Daten innerhalb eines JARs zu kommen, muss man den InputStream laden,
			 * nicht getFile() benutzen, da diese auf das Dateisystem geht
			 */
			InputStream stream = new FileInputStream("./config/mobileDevices.properties");
			movileDevicesProps.load(stream);
			LOGGER.debug("Loaded " + movileDevicesProps.entrySet().size() + " MobileDevices from properties");
		} catch (IOException e) {
			LOGGER.error("Could not read MobileDevice-Properties file.", e);
		}
		return movileDevicesProps;
	}
}
