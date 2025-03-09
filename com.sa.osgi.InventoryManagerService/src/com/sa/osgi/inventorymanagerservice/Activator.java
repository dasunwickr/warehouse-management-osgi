package com.sa.osgi.inventorymanagerservice;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sa.osgi.barcodescannerservice.IBarcodeScanner;
import com.sa.osgi.weightsensorservice.IPackageWeightSensor;

import java.util.Map;

public class Activator implements BundleActivator {

    private ServiceReference<IBarcodeScanner> barcodeScannerRef;
    private ServiceReference<IPackageWeightSensor> weightSensorRef;

    @Override
    public void start(BundleContext context) throws Exception {
        // Get references to producer services
        barcodeScannerRef = context.getServiceReference(IBarcodeScanner.class);
        weightSensorRef = context.getServiceReference(IPackageWeightSensor.class);

        if (barcodeScannerRef != null && weightSensorRef != null) {
            IBarcodeScanner barcodeScanner = context.getService(barcodeScannerRef);
            IPackageWeightSensor weightSensor = context.getService(weightSensorRef);

            System.out.println("[Inventory Manager] Starting inventory processing...");

            // Retrieve packages and display inventory
            Map<String, String> packages = barcodeScanner.getAllPackages(); // Retrieve all packages
            for (Map.Entry<String, String> entry : packages.entrySet()) {
                String packageId = entry.getKey();
                String productName = entry.getValue();
                double weight = weightSensor.getWeight(packageId);

                System.out.println("📦 Package ID: " + packageId + ", Product: " + productName + ", Weight: " + weight + " kg");
            }

            System.out.println("[Inventory Manager] Inventory processing completed.");
        } else {
            System.out.println("Required services are not available.");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // Unget services
        if (barcodeScannerRef != null) {
            context.ungetService(barcodeScannerRef);
        }
        if (weightSensorRef != null) {
            context.ungetService(weightSensorRef);
        }

        System.out.println("Inventory Manager Service stopped.");
    }
}