package com.sa.osgi.barcodescannerservice;

import com.sa.osgi.barcodescannerservice.impl.BarcodeScannerImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private BarcodeScannerImpl barcodeScanner;

    @Override
    public void start(BundleContext context) throws Exception {
        barcodeScanner = new BarcodeScannerImpl();
        barcodeScanner.start(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        barcodeScanner.stop();
    }
}