package com.sa.osgi.shippingcostcalculatorservice;

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
        orderProcessingRef = context.getServiceReference(IOrderProcessing.class);
        weightSensorRef = context.getServiceReference(IPackageWeightSensor.class);

        if (orderProcessingRef != null && weightSensorRef != null) {
            IOrderProcessing orderProcessing = context.getService(orderProcessingRef);
            IPackageWeightSensor weightSensor = context.getService(weightSensorRef);

            System.out.println("[Shipping Cost Calculator] Starting calculations...");

            Map<String, Double> orders = orderProcessing.getAllOrders();
            for (Map.Entry<String, Double> entry : orders.entrySet()) {
                String orderId = entry.getKey();
                double baseWeight = entry.getValue();
                double packageWeight = weightSensor.getWeight(orderId);

                double totalWeight = baseWeight + packageWeight;
                double shippingCost = totalWeight * 2; // $2 per kg

                System.out.println("📦 Order ID: " + orderId + ", Total Weight: " + totalWeight + " kg, Shipping Cost: $" + shippingCost);
            }

            System.out.println("[Shipping Cost Calculator] Calculations completed.");
        } else {
            System.out.println("Required services are not available.");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (orderProcessingRef != null) {
            context.ungetService(orderProcessingRef);
        }
        if (weightSensorRef != null) {
            context.ungetService(weightSensorRef);
        }

        System.out.println("Shipping Cost Calculator Service stopped.");
    }
}