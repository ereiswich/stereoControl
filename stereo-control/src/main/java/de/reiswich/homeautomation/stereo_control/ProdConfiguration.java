package de.reiswich.homeautomation.stereo_control;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import de.reiswich.homeautomation.stereo_control.light.LightSwitch;
import de.reiswich.homeautomation.stereo_control.stereo.MPCRadioPlayer;

@Configuration
@PropertySource("classpath:application.properties")
public class ProdConfiguration {
	
	@Value( "${de.reiswich.mpd.server}" )
	private String mpdServerIp;
	
	@Value( "${de.reiswich.mpd.port}" )
	private int mpdServerPort;

	private Logger logger = LoggerFactory.getLogger(ProdConfiguration.class.getName());

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	@Bean
	public MPCRadioPlayer getMpcRadioPlayer() {
		return new MPCRadioPlayer(mpdServerIp, mpdServerPort);
	}

	@Bean
	public RadioController getRadioController() {
		logger.debug("initializing RadioController");
		RadioController radioController = new RadioController(getMobileDevicesProperties(), getLightSwitch(), getMpcRadioPlayer());
		radioController.init();
		logger.debug("RadioController initialized");
		return radioController;
	}


	@Bean
	public LightSwitch getLightSwitch() {
		logger.debug("initializing LightSwitch");
		return new LightSwitch();
	}

	private Properties getMobileDevicesProperties() {
		logger.debug("Trying to read MobileDevice-Properties");
		Properties movileDevicesProps = new Properties();
		try {
			/*
			 * um an Daten innerhalb eines JARs zu kommen, muss man den InputStream laden,
			 * nicht getFile() benutzen, da diese auf das Dateisystem geht
			 */
			InputStream stream = new FileInputStream("./config/mobileDevices.properties");
			movileDevicesProps.load(stream);
			logger.debug("Loaded " + movileDevicesProps.entrySet().size() + " MobileDevices from properties");
		} catch (IOException e) {
			logger.error("Could not read MobileDevice-Properties file.", e);
		}
		return movileDevicesProps;
	}
}
