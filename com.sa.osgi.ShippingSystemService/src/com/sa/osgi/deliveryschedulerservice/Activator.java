package com.sa.osgi.deliveryschedulerservice;

import com.sa.osgi.orderprocessingservice.IOrderProcessing;
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

    private ServiceReference<IOrderProcessing> orderProcessingRef;
    private ServiceReference<IPackageWeightSensor> weightSensorRef;

    @Override
    public void start(BundleContext context) throws Exception {
        // Get references to producer services
        orderProcessingRef = context.getServiceReference(IOrderProcessing.class);
        weightSensorRef = context.getServiceReference(IPackageWeightSensor.class);

        if (orderProcessingRef != null && weightSensorRef != null) {
            IOrderProcessing orderProcessing = context.getService(orderProcessingRef);
            IPackageWeightSensor weightSensor = context.getService(weightSensorRef);

            System.out.println("[Delivery Scheduler] Starting delivery scheduling...");

            // Read orders from a text file
            Map<String, Double> orders = readDataFromFile("D:/projects/sliit/y3s2/sa/warehouse-management-osgi/data/orders.txt");

            // Schedule deliveries for each order
            for (Map.Entry<String, Double> entry : orders.entrySet()) {
                String orderId = entry.getKey();
                double orderWeight = entry.getValue();

                // Retrieve additional package weights dynamically
                double totalWeight = orderWeight; // Base weight
                System.out.println("📦 Order ID: " + orderId + ", Base Weight: " + orderWeight + " kg");

                // Add package weights dynamically (if needed)
                // Example: Simulate retrieving package weights from WeightSensorService
                double packageWeight = weightSensor.getWeight(orderId);
                totalWeight += packageWeight;
                System.out.println("📦 Additional Package Weight: " + packageWeight + " kg");

                // Calculate shipping cost ($2 per kg)
                double shippingCost = totalWeight * 2;
                System.out.println("🚚 Shipping cost for Order ID " + orderId + ": $" + shippingCost);

                // Determine delivery type based on shipping cost
                String deliveryType = shippingCost > 15 ? "Priority Shipping" : "Standard Shipping";
                System.out.println("🚚 Scheduled delivery for Order ID " + orderId + ": " + deliveryType + " ($" + shippingCost + ")");
            }

            System.out.println("[Delivery Scheduler] Delivery scheduling completed.");
        } else {
            System.out.println("Required services are not available.");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // Unget services
        if (orderProcessingRef != null) {
            context.ungetService(orderProcessingRef);
        }
        if (weightSensorRef != null) {
            context.ungetService(weightSensorRef);
        }

        System.out.println("Delivery Scheduler Service stopped.");
    }

    /**
     * Reads data from a text file in the format "id:weight".
     */
    private Map<String, Double> readDataFromFile(String filePath) {
        Map<String, Double> data = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String id = parts[0].trim();
                    double weight = Double.parseDouble(parts[1].trim());
                    data.put(id, weight);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
            e.printStackTrace();
        }
        return data;
    }
}