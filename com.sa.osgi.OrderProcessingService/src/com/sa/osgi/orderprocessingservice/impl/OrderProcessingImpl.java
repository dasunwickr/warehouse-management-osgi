package com.sa.osgi.orderprocessingservice.impl;

import com.sa.osgi.orderprocessingservice.IOrderProcessing;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

public class OrderProcessingImpl implements IOrderProcessing {

    private static final String DATA_DIR = "D:/3rd year/warehouse-management-osgi/data";
    private static final String DATA_FILE = DATA_DIR + "/orders.txt";

    private final Map<String, Order> orders = new HashMap<>();
    private ServiceRegistration<IOrderProcessing> registration;

    private static final Logger LOGGER = Logger.getLogger(OrderProcessingImpl.class.getName());

    public void start(BundleContext context) {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists() && !dataDir.mkdirs()) {
            LOGGER.severe("Failed to create directory: " + dataDir.getAbsolutePath());
            return;
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
    public void processOrder(String orderId) {
        if (orders.containsKey(orderId)) {
            orders.get(orderId).setStatus("Processed");
            saveOrders();
            System.out.println("\uD83D\uDCE6 Order processed: " + orderId);
        } else {
            LOGGER.warning("Order ID not found: " + orderId);
        }
    }

    @Override
    public double getOrderWeight(String orderId) {
        return orders.getOrDefault(orderId, new Order()).getWeight();
    }

    @Override
    public void addOrder(String orderId, double weight) {
        if (orderId == null || orderId.trim().isEmpty()) {
            LOGGER.warning("Order ID cannot be empty.");
            return;
        }
        if (weight <= 0) {
            LOGGER.warning("Weight must be greater than zero.");
            return;
        }

        Order order = new Order();
        order.setWeight(weight);
        order.setStatus("Pending");
        orders.put(orderId, order);
        saveOrders();
        System.out.println("\uD83D\uDCE6 Order added: " + orderId + ", Weight: " + weight);
    }

    private void loadOrders() {
        File file = new File(DATA_FILE);
        try {
            ensureFileExists(file);
            loadFromFile(file);
        } catch (IOException e) {
            LOGGER.severe("Error handling file: " + file.getAbsolutePath());
        }
    }

    private void saveOrders() {
        File file = new File(DATA_FILE);
        try {
            saveToFile(file);
        } catch (IOException e) {
            LOGGER.severe("Error saving data to file: " + file.getAbsolutePath());
        }
    }

    private void startScanner() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("[Order Processing] Starting interactive mode. Type 'exit' to quit.");

        while (true) {
            System.out.print("[Order Processing] Enter order ID: ");
            String orderId = scanner.nextLine().trim();
            if (orderId.equalsIgnoreCase("exit")) break;

            System.out.print("[Order Processing] Enter weight (kg): ");
            double weight;
            try {
                weight = Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid weight input.");
                continue;
            }

            addOrder(orderId, weight);
        }
        System.out.println("[Order Processing] Interactive mode stopped.");
    }

    private void ensureFileExists(File file) throws IOException {
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Failed to create file: " + file.getAbsolutePath());
        }
    }

    private void loadFromFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    Order order = new Order();
                    order.setWeight(Double.parseDouble(parts[1]));
                    order.setStatus(parts[2]);
                    orders.put(parts[0], order);
                }
            }
        }
    }

    private void saveToFile(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, Order> entry : orders.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue().getWeight() + ":" + entry.getValue().getStatus());
                writer.newLine();
            }
        }
    }

    private static class Order {
        private double weight;
        private String status;

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}