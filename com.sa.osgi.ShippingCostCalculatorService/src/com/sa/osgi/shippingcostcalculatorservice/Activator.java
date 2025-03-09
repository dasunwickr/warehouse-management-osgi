package com.sa.osgi.shippingcostcalculatorservice;

import com.sa.osgi.orderprocessingservice.IOrderProcessing;
import com.sa.osgi.weightsensorservice.IPackageWeightSensor;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.Scanner;

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

            // Start interactive input
            Scanner scanner = new Scanner(System.in);
            System.out.println("[Shipping Cost Calculator] Starting interactive mode. Type 'exit' to quit.");

            while (true) {
                System.out.print("[Shipping Cost Calculator] Enter order ID: ");
                String orderId = scanner.nextLine();
                if (orderId.equalsIgnoreCase("exit")) break;

                double orderWeight = orderProcessing.getOrderWeight(orderId);
                System.out.println("📦 Order weight: " + orderWeight + " kg");

                System.out.print("[Shipping Cost Calculator] Enter package ID: ");
                String packageId = scanner.nextLine();
                if (packageId.equalsIgnoreCase("exit")) break;

                double packageWeight = weightSensor.getWeight(packageId);
                System.out.println("📦 Package weight: " + packageWeight + " kg");

                // Calculate shipping cost
                double totalWeight = orderWeight + packageWeight;
                double shippingCost = calculateShippingCost(totalWeight);
                System.out.println("🚚 Shipping cost: $" + shippingCost);
            }

            System.out.println("[Shipping Cost Calculator] Interactive mode stopped.");
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

        System.out.println("Shipping Cost Calculator Service stopped.");
    }

    /**
     * Calculates the shipping cost based on total weight.
     * Example formula: $2 per kg.
     */
    private double calculateShippingCost(double totalWeight) {
        return totalWeight * 2.0; // $2 per kg
    }
}