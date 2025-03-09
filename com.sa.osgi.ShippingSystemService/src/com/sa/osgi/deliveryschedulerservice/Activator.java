package com.sa.osgi.deliveryschedulerservice;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sa.osgi.orderprocessingservice.IOrderProcessing;
import com.sa.osgi.weightsensorservice.IPackageWeightSensor;

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
            System.out.println("[Delivery Scheduler] Starting interactive mode. Type 'exit' to quit.");

            while (true) {
                System.out.print("[Delivery Scheduler] Enter order ID: ");
                String orderId = scanner.nextLine();
                if (orderId.equalsIgnoreCase("exit")) break;

                double weight = orderProcessing.getOrderWeight(orderId);
                System.out.println("🚚 Order weight: " + weight);

                System.out.print("[Delivery Scheduler] Enter package ID: ");
                String packageId = scanner.nextLine();
                if (packageId.equalsIgnoreCase("exit")) break;

                double packageWeight = weightSensor.getWeight(packageId);
                System.out.println("🚚 Package weight: " + packageWeight);
            }

            System.out.println("[Delivery Scheduler] Interactive mode stopped.");
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