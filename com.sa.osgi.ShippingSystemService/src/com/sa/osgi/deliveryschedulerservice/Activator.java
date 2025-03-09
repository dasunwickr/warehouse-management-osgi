package com.sa.osgi.deliveryschedulerservice;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sa.osgi.orderprocessingservice.IOrderProcessing;
import com.sa.osgi.weightsensorservice.IPackageWeightSensor;

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

            // Retrieve orders and generate delivery schedules
            Map<String, Double> orders = orderProcessing.getAllOrders(); // Retrieve all orders
            for (Map.Entry<String, Double> entry : orders.entrySet()) {
                String orderId = entry.getKey();
                double amount = entry.getValue();

                // Calculate total weight using WeightSensorService
                double packageWeight = weightSensor.getWeight(orderId);
                double totalWeight = amount + packageWeight;

                // Determine delivery type based on total weight
                String deliveryType = totalWeight > 15 ? "Priority Shipping" : "Standard Shipping";

                System.out.println("🚚 Scheduled delivery for Order ID " + orderId + ": " + deliveryType + " (Total Weight: " + totalWeight + " kg)");
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
}