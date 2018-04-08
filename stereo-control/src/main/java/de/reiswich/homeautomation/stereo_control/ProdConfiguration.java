package de.reiswich.homeautomation.stereo_control;

import java.util.Iterator;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.ResourcePropertySource;

import de.reiswich.homeautomation.stereo_control.light.LightSwitch;
import de.reiswich.homeautomation.stereo_control.light.SunsetService;

@Configuration
@PropertySource("classpath:mobileDevices.properties")
public class ProdConfiguration {

	@Autowired
	private Environment env;

	//test
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public RadioController getRadioController() {
		RadioController radioController = new RadioController(getMobileDevicesProperties(), getLightSwitch());
		radioController.init();
		return radioController;
	}
	
	@Bean
	public SunsetService getSunsetService() {
		return new SunsetService();
	}
	
	@Bean
	public LightSwitch getLightSwitch() {
		return new LightSwitch(getSunsetService());
	}

	private Properties getMobileDevicesProperties() {
		Properties movileDevices = new Properties();
		for (Iterator<?> it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext();) {
			Object propertySource = it.next();
			if (propertySource instanceof ResourcePropertySource) {
				movileDevices.putAll(((ResourcePropertySource) propertySource).getSource());
			}
		}
		return movileDevices;
	}
}
