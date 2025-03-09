package com.sa.osgi.inventorymanagerservice;

import com.sa.osgi.barcodescannerservice.IBarcodeScanner;
import com.sa.osgi.weightsensorservice.IPackageWeightSensor;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
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

            // Read package IDs from a text file
            Map<String, String> packages = readDataFromFile("D:/projects/sliit/y3s2/sa/warehouse-management-osgi/data/packages.txt");

            // Process each package ID
            for (Map.Entry<String, String> entry : packages.entrySet()) {
                String packageId = entry.getKey();
                String product = barcodeScanner.scanPackage(packageId);
                double weight = weightSensor.getWeight(packageId);

                System.out.println("📦 Package ID: " + packageId + ", Product: " + product + ", Weight: " + weight + " kg");
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

    /**
     * Reads data from a text file in the format "id:product".
     */
    private Map<String, String> readDataFromFile(String filePath) {
        Map<String, String> data = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String id = parts[0].trim();
                    String product = parts[1].trim();
                    data.put(id, product);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
            e.printStackTrace();
        }
        return data;
    }
}