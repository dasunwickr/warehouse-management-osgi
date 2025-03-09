package com.sa.osgi.barcodescannerservice.impl;

import com.sa.osgi.barcodescannerservice.IBarcodeScanner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

public class BarcodeScannerImpl implements IBarcodeScanner {

    private static final String DATA_DIR = "D:/projects/sliit/y3s2/sa/warehouse-management-osgi/data";
    private static final String DATA_FILE = DATA_DIR + "/barcodes.txt";

    private Map<String, String> packages = new HashMap<>();
    private ServiceRegistration<IBarcodeScanner> registration;

    private static final Logger LOGGER = Logger.getLogger(BarcodeScannerImpl.class.getName());

    public void start(BundleContext context) {
        // Ensure the 'data' directory exists
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

        // Start the scanner in the main thread
        startScanner();
    }

    public void stop() {
        savePackages();
        registration.unregister();
        LOGGER.info("Barcode Scanner Service stopped.");
    }

    @Override
    public String scanPackage(String packageId) {
        return packages.getOrDefault(packageId, "UNKNOWN");
    }

    @Override
    public void addPackage(String id, String product) {
        if (id == null || id.trim().isEmpty()) {
            LOGGER.warning("Package ID cannot be empty.");
            return;
        }
        if (product == null || product.trim().isEmpty()) {
            LOGGER.warning("Product name cannot be empty.");
            return;
        }

        packages.put(id, product);
        savePackages();
        System.out.println("📦 Package added: " + id + " - " + product);
    }

    private void loadPackages() {
        File file = new File(DATA_FILE);

        // If the file doesn't exist, create it
        try {
            ensureFileExists(file);
        } catch (IOException e) {
            LOGGER.severe("Error creating file: " + file.getAbsolutePath());
            return;
        }

        // Load data from the file
        try {
            loadFromFile(file);
        } catch (IOException e) {
            LOGGER.severe("Error loading data from file: " + file.getAbsolutePath());
        }
    }

    private void savePackages() {
        File file = new File(DATA_FILE);

        // Save data to the file
        try {
            saveToFile(file);
        } catch (IOException e) {
            LOGGER.severe("Error saving data to file: " + file.getAbsolutePath());
        }
    }

    private void startScanner() {
        Scanner scanner = new Scanner(System.in); // Do NOT close this scanner
        System.out.println("[Barcode Scanner] Starting interactive mode. Type 'exit' to quit.");

        while (true) {
            System.out.print("[Barcode Scanner] Enter package ID: ");
            String id = scanner.nextLine();
            if (id.equalsIgnoreCase("exit")) break;

            System.out.print("[Barcode Scanner] Enter product name: ");
            String product = scanner.nextLine();
            addPackage(id, product);
        }

        System.out.println("[Barcode Scanner] Interactive mode stopped.");
        // DO NOT CLOSE THE SCANNER HERE
    }

    private void ensureFileExists(File file) throws IOException {
        if (!file.exists()) {
            boolean success = file.createNewFile();
            if (!success) {
                throw new IOException("Failed to create file: " + file.getAbsolutePath());
            }
        }
    }

    private void loadFromFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    packages.put(parts[0], parts[1]);
                }
            }
        }
    }

    private void saveToFile(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, String> entry : packages.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        }
    }
}