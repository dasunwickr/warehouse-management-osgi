package com.sa.osgi.orderprocessingservice.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sa.osgi.orderprocessingservice.IOrderProcessing;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

public class OrderProcessingImpl implements IOrderProcessing {

    private static final String DATA_DIR = "D:/projects/sliit/y3s2/sa/warehouse-management-osgi/data";
    private static final String DATA_FILE = DATA_DIR + "/orders.txt";

    private Map<String, Double> orders = new HashMap<>();
    private ServiceRegistration<IOrderProcessing> registration;

    private static final Logger LOGGER = Logger.getLogger(OrderProcessingImpl.class.getName());

    public void start(BundleContext context) {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            boolean success = dataDir.mkdirs();
            if (!success) {
                LOGGER.severe("Failed to create directory: " + dataDir.getAbsolutePath());
                return;
            }
        }
        LOGGER.info("Data directory available: " + dataDir.getAbsolutePath());

        loadOrders();
        registration = context.registerService(IOrderProcessing.class, this, null);
        LOGGER.info("Order Processing Service started.");

        startScanner();
    }

    public void stop() {
        saveOrders();
        registration.unregister();
        LOGGER.info("Order Processing Service stopped.");
    }

    @Override
    public double getOrderAmount(String orderId) {
        return orders.getOrDefault(orderId, 0.0);
    }

    @Override
    public void addOrder(String orderId, double amount) {
        if (orderId == null || orderId.trim().isEmpty() || amount <= 0) {
            LOGGER.warning("Invalid order ID or amount.");
            return;
        }

        // Add the order to memory and save it to the file
        orders.put(orderId, amount);
        saveOrders();

        System.out.println("📦 Order added: " + orderId + ", Amount: $" + amount);
    }

    @Override
    public Map<String, Double> getAllOrders() {
        return new HashMap<>(orders); // Return a copy of the orders map to avoid direct modification
    }

    private void loadOrders() {
        File file = new File(DATA_FILE);
        try {
            ensureFileExists(file);
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        orders.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Error loading data from file: " + file.getAbsolutePath());
        }
    }

    private void saveOrders() {
        File file = new File(DATA_FILE);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, Double> entry : orders.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            LOGGER.severe("Error saving data to file: " + file.getAbsolutePath());
        }
    }

    private void startScanner() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("[Order Processing] Starting interactive mode. Type 'exit' to quit.");
        while (true) {
            System.out.print("[Order Processing] Enter order ID: ");
            String orderId = scanner.nextLine();
            if (orderId.equalsIgnoreCase("exit")) break;

            System.out.print("[Order Processing] Enter order amount ($): ");
            double amount = Double.parseDouble(scanner.nextLine());

            addOrder(orderId, amount);
        }
        System.out.println("[Order Processing] Interactive mode stopped.");
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