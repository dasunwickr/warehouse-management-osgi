package com.sa.osgi.workermobileappservice;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.osgi.taskschedulerservice.ITaskScheduler;
import com.sa.osgi.barcodescannerservice.IBarcodeScanner;
import com.sa.osgi.orderprocessingservice.IOrderProcessing;
import com.sa.osgi.weightsensorservice.IPackageWeightSensor;

import java.util.Map;

public class Activator implements BundleActivator {

    private ServiceReference<IBarcodeScanner> barcodeScannerRef;
    private ServiceReference<IOrderProcessing> orderProcessingRef;
    private ServiceReference<IPackageWeightSensor> weightSensorRef;
    private ServiceReference<ITaskScheduler> taskSchedulerRef;

    @Override
    public void start(BundleContext context) throws Exception {
        // Get references to producer services
        barcodeScannerRef = context.getServiceReference(IBarcodeScanner.class);
        orderProcessingRef = context.getServiceReference(IOrderProcessing.class);
        weightSensorRef = context.getServiceReference(IPackageWeightSensor.class);
        taskSchedulerRef = context.getServiceReference(ITaskScheduler.class);

        if (barcodeScannerRef != null && orderProcessingRef != null && weightSensorRef != null && taskSchedulerRef != null) {
            IBarcodeScanner barcodeScanner = context.getService(barcodeScannerRef);
            IOrderProcessing orderProcessing = context.getService(orderProcessingRef);
            IPackageWeightSensor weightSensor = context.getService(weightSensorRef);
            ITaskScheduler taskScheduler = context.getService(taskSchedulerRef);

            System.out.println("[Worker Mobile App] Starting task assignment...");

            // Step 1: Retrieve all packages
            Map<String, String> packages = barcodeScanner.getAllPackages(); // Retrieve all packages
            for (Map.Entry<String, String> packageEntry : packages.entrySet()) {
                String packageId = packageEntry.getKey();
                String productName = packageEntry.getValue();

                // Step 2: Retrieve package weight
                double weight = weightSensor.getWeight(packageId);

                // Step 3: Check if there's an order for this package
                Map<String, Double> orders = orderProcessing.getAllOrders(); // Retrieve all orders
                boolean hasOrder = false;
                for (Map.Entry<String, Double> orderEntry : orders.entrySet()) {
                    String orderId = orderEntry.getKey();
                    double amount = orderEntry.getValue();

                    if (orderId.equals(packageId)) {
                        hasOrder = true;

                        // Step 4: Assign a task to an employee
                        String taskDescription = "Process order " + orderId + " for " + productName +
                                " (Amount: $" + amount + ", Weight: " + weight + " kg)";
                        taskScheduler.assignTask("EMP001", taskDescription); // Default assignment to EMP001
                        System.out.println("📝 Task assigned for order: " + orderId);
                        break;
                    }
                }

                if (!hasOrder) {
                    System.out.println("⚠️ No order found for package ID: " + packageId);
                }
            }

            System.out.println("[Worker Mobile App] Task assignment completed.");
        } else {
            System.out.println("Required services are not available.");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // Unget services
        if (barcodeScannerRef != null) {
            context.ungetService(barcodeScannerRef);
        }
        if (orderProcessingRef != null) {
            context.ungetService(orderProcessingRef);
        }
        if (weightSensorRef != null) {
            context.ungetService(weightSensorRef);
        }
        if (taskSchedulerRef != null) {
            context.ungetService(taskSchedulerRef);
        }

        System.out.println("Worker Mobile App Service stopped.");
    }
}