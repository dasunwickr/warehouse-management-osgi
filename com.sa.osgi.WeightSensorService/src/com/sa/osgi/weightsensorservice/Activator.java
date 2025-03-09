package com.sa.osgi.weightsensorservice;

import com.sa.osgi.weightsensorservice.impl.WeightSensorImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private WeightSensorImpl weightSensor;

    @Override
    public void start(BundleContext context) throws Exception {
        weightSensor = new WeightSensorImpl();
        weightSensor.start(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        weightSensor.stop();
    }
}