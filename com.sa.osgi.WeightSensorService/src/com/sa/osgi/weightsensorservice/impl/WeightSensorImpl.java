package com.sa.osgi.weightsensorservice.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sa.osgi.weightsensorservice.IPackageWeightSensor;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

public class WeightSensorImpl implements IPackageWeightSensor {

    private static final String DATA_DIR = "D:/projects/sliit/y3s2/sa/warehouse-management-osgi/data";
    private static final String DATA_FILE = DATA_DIR + "/weights.txt";

    private Map<String, Double> weights = new HashMap<>();
    private ServiceRegistration<IPackageWeightSensor> registration;

    private static final Logger LOGGER = Logger.getLogger(WeightSensorImpl.class.getName());

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

        // Load existing weights from the file
        loadWeights();

        // Register the service with the OSGi framework
        registration = context.registerService(IPackageWeightSensor.class, this, null);
        LOGGER.info("Package Weight Sensor Service started.");

        // Start interactive mode for recording weights
        startScanner();
    }

    public void stop() {
        // Save weights to the file before stopping
        saveWeights();

        // Unregister the service
        registration.unregister();
        LOGGER.info("Package Weight Sensor Service stopped.");
    }

    @Override
    public double getWeight(String packageId) {
        return weights.getOrDefault(packageId, 0.0);
    }

    @Override
    public void recordWeight(String packageId, double weight) {
        if (packageId == null || packageId.trim().isEmpty()) {
            LOGGER.warning("Package ID cannot be empty.");
            return;
        }
        if (weight <= 0) {
            LOGGER.warning("Weight must be greater than zero.");
            return;
        }

        // Record the weight in memory and save it to the file
        weights.put(packageId, weight);
        saveWeights();
        System.out.println("⚖️ Weight recorded for package: " + packageId + ", Weight: " + weight);
    }

    private void loadWeights() {
        File file = new File(DATA_FILE);

        // If the file doesn't exist, create it
        try {
            ensureFileExists(file);
        } catch (IOException e) {
            LOGGER.severe("Error creating file: " + file.getAbsolutePath());
            return;
        }

        // Load data from the file into memory
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String packageId = parts[0].trim();
                    double weight = Double.parseDouble(parts[1].trim());
                    weights.put(packageId, weight);
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Error loading data from file: " + file.getAbsolutePath());
        }
    }

    private void saveWeights() {
        File file = new File(DATA_FILE);

        // Save data from memory to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, Double> entry : weights.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            LOGGER.severe("Error saving data to file: " + file.getAbsolutePath());
        }
    }

    private void startScanner() {
        Scanner scanner = new Scanner(System.in); // Do NOT close this scanner
        System.out.println("[Weight Sensor] Starting interactive mode. Type 'exit' to quit.");

        while (true) {
            System.out.print("[Weight Sensor] Enter package ID: ");
            String packageId = scanner.nextLine();
            if (packageId.equalsIgnoreCase("exit")) break;

            System.out.print("[Weight Sensor] Enter weight (kg): ");
            double weight = Double.parseDouble(scanner.nextLine());
            recordWeight(packageId, weight);
        }

        System.out.println("[Weight Sensor] Interactive mode stopped.");
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
}