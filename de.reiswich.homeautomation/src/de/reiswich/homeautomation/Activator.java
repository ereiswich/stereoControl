package de.reiswich.homeautomation;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private Logger logger = Logger.getLogger(Activator.class);

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		logger.info("Starting home automation application...");
		
		Properties props = new Properties();
		InputStream in = null;
		try{
			in = getClass().getClassLoader().getResourceAsStream("mobileDevices.properties");
			props.load(in);
			logger.info("Mobile device properties file found");
		}finally{
			in.close();
		}

		RadioController radioController = new RadioController(props);
		radioController.init();

		context.registerService(CommandProvider.class, radioController, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
