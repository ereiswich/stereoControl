package de.reiswich.homeautomation.plantwatering;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class PlantWateringActivator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		PlantWateringActivator.context = bundleContext;
		
		MoistureSensor sensor = new MoistureSensor();
		sensor.readSensor();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		PlantWateringActivator.context = null;
	}

}
