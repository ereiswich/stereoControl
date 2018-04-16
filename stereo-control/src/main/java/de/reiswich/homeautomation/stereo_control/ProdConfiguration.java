package de.reiswich.homeautomation.stereo_control;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import de.reiswich.homeautomation.stereo_control.light.LightSwitch;
import de.reiswich.homeautomation.stereo_control.light.SunsetService;

@Configuration
public class ProdConfiguration {

	private Logger logger = LoggerFactory.getLogger(ProdConfiguration.class.getName());

	@Autowired
	private ResourceLoader resourceLoader;

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public RadioController getRadioController() {
		logger.debug("initializing RadioController");
		RadioController radioController = new RadioController(getMobileDevicesProperties(), getLightSwitch());
		radioController.init();
		logger.debug("RadioController initialized");
		return radioController;
	}

	@Bean
	public SunsetService getSunsetService() {
		logger.debug("initializing SunsetService");
		return new SunsetService();
	}

	@Bean
	public LightSwitch getLightSwitch() {
		logger.debug("initializing LightSwitch");
		return new LightSwitch(getSunsetService());
	}

	private Properties getMobileDevicesProperties() {
		logger.debug("Trying to read MobileDevice-Properties");
		Properties movileDevicesProps = new Properties();
		try {
			Resource resource = resourceLoader.getResource("classpath:mobileDevices.properties");
			/*
			 * um an Daten innerhalb eines JARs zu kommen, muss man den InputStream laden,
			 * nicht getFile() benutzen, da diese auf das Dateisystem geht
			 */
			InputStream stream = resource.getInputStream();
			movileDevicesProps.load(stream);
			logger.debug("Loaded " + movileDevicesProps.entrySet().size() + " MobileDevices from properties");
		} catch (IOException e) {
			logger.error("Could not read MobileDevice-Properties file.", e);
		}
		return movileDevicesProps;
	}
}
