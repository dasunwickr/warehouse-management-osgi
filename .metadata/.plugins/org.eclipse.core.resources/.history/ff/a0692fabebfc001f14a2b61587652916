package com.sa.osgi.barcodescannerservice;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        System.out.println("Barcode Scanner Service started.");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
        System.out.println("Barcode Scanner Service stopped.");
    }
}