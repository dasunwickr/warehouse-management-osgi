package com.sa.osgi.workermobileappservice;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.osgi.taskschedulerservice.ITaskScheduler;
import com.sa.osgi.orderprocessingservice.IOrderProcessing;

import java.util.Scanner;

public class Activator implements BundleActivator {

    private ServiceReference<ITaskScheduler> taskSchedulerRef;
    private ServiceReference<IOrderProcessing> orderProcessingRef;

    @Override
    public void start(BundleContext context) throws Exception {
        // Get references to producer services
        taskSchedulerRef = context.getServiceReference(ITaskScheduler.class);
        orderProcessingRef = context.getServiceReference(IOrderProcessing.class);

        if (taskSchedulerRef != null && orderProcessingRef != null) {
            ITaskScheduler taskScheduler = context.getService(taskSchedulerRef);
            IOrderProcessing orderProcessing = context.getService(orderProcessingRef);

            // Start interactive input
            Scanner scanner = new Scanner(System.in);
            System.out.println("[Worker Mobile App] Starting interactive mode. Type 'exit' to quit.");

            while (true) {
                System.out.print("[Worker Mobile App] Enter employee ID: ");
                String empId = scanner.nextLine();
                if (empId.equalsIgnoreCase("exit")) break;

                System.out.print("[Worker Mobile App] Enter task: ");
                String task = scanner.nextLine();
                taskScheduler.assignTask(empId, task);
                System.out.println("📱 Task assigned to " + empId + ": " + task);

                System.out.print("[Worker Mobile App] Enter order ID: ");
                String orderId = scanner.nextLine();
                if (orderId.equalsIgnoreCase("exit")) break;

                double weight = orderProcessing.getOrderWeight(orderId);
                System.out.println("📱 Order weight: " + weight);
            }

            System.out.println("[Worker Mobile App] Interactive mode stopped.");
        } else {
            System.out.println("Required services are not available.");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // Unget services
        if (taskSchedulerRef != null) {
            context.ungetService(taskSchedulerRef);
        }
        if (orderProcessingRef != null) {
            context.ungetService(orderProcessingRef);
        }

        System.out.println("Worker Mobile App Service stopped.");
    }
}