package com.sa.osgi.barcodescannerservice.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sa.osgi.barcodescannerservice.IBarcodeScanner;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

public class BarcodeScannerImpl implements IBarcodeScanner {

    private static final String DATA_DIR = "D:/projects/sliit/y3s2/sa/warehouse-management-osgi/data";
    private static final String DATA_FILE = DATA_DIR + "/packages.txt";

    private Map<String, String> packages = new HashMap<>();
    private ServiceRegistration<IBarcodeScanner> registration;

    private static final Logger LOGGER = Logger.getLogger(BarcodeScannerImpl.class.getName());

    public void start(BundleContext context) {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            boolean success = dataDir.mkdirs();
            if (!success) {
                LOGGER.severe("Failed to create directory: " + dataDir.getAbsolutePath());
                return;
            }
        }
        LOGGER.info("Data directory created: " + dataDir.exists());

        loadPackages();
        registration = context.registerService(IBarcodeScanner.class, this, null);
        LOGGER.info("Barcode Scanner Service started.");

        startScanner();
    }

    public void stop() {
        savePackages();
        registration.unregister();
        LOGGER.info("Barcode Scanner Service stopped.");
    }

    @Override
    public String scanPackage(String packageId) {
        return packages.getOrDefault(packageId, "Unknown Package");
    }

    @Override
    public void addPackage(String packageId, String productName) {
        if (packageId == null || packageId.trim().isEmpty() || productName == null || productName.trim().isEmpty()) {
            LOGGER.warning("Invalid package ID or product name.");
            return;
        }
        packages.put(packageId, productName);
        savePackages();
        System.out.println("📦 Package added: " + packageId + " - " + productName);
    }

    private void loadPackages() {
        File file = new File(DATA_FILE);
        try {
            ensureFileExists(file);
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        packages.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Error loading data from file: " + file.getAbsolutePath());
        }
    }

    private void savePackages() {
        File file = new File(DATA_FILE);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, String> entry : packages.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            LOGGER.severe("Error saving data to file: " + file.getAbsolutePath());
        }
    }

    private void startScanner() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("[Barcode Scanner] Starting interactive mode. Type 'exit' to quit.");
        while (true) {
            System.out.print("[Barcode Scanner] Enter package ID: ");
            String packageId = scanner.nextLine();
            if (packageId.equalsIgnoreCase("exit")) break;

            System.out.print("[Barcode Scanner] Enter product name: ");
            String productName = scanner.nextLine();
            addPackage(packageId, productName);
        }
        System.out.println("[Barcode Scanner] Interactive mode stopped.");
    }

    private void ensureFileExists(File file) throws IOException {
        if (!file.exists()) {
            boolean success = file.createNewFile();
            if (!success) {
                throw new IOException("Failed to create file: " + file.getAbsolutePath());
            }
        }
    }
}