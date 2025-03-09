package com.sa.osgi.workermobileappservice;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.osgi.taskschedulerservice.ITaskScheduler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Activator implements BundleActivator {

    private ServiceReference<ITaskScheduler> taskSchedulerRef;

    @Override
    public void start(BundleContext context) throws Exception {
        // Get reference to producer service
        taskSchedulerRef = context.getServiceReference(ITaskScheduler.class);

        if (taskSchedulerRef != null) {
            ITaskScheduler taskScheduler = context.getService(taskSchedulerRef);

            System.out.println("[Worker Mobile App] Starting task assignment...");

            // Read employee tasks from a text file
            Map<String, String> tasks = readDataFromFile("D:/projects/sliit/y3s2/sa/warehouse-management-osgi/data/tasks.txt");

            // Assign tasks to employees
            for (Map.Entry<String, String> entry : tasks.entrySet()) {
                String empId = entry.getKey();
                String task = entry.getValue();
                taskScheduler.assignTask(empId, task);
                System.out.println("📱 Task assigned to Employee ID " + empId + ": " + task);
            }

            System.out.println("[Worker Mobile App] Task assignment completed.");
        } else {
            System.out.println("Required services are not available.");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // Unget service
        if (taskSchedulerRef != null) {
            context.ungetService(taskSchedulerRef);
        }

        System.out.println("Worker Mobile App Service stopped.");
    }

    /**
     * Reads data from a text file in the format "empId:task".
     */
    private Map<String, String> readDataFromFile(String filePath) {
        Map<String, String> data = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String empId = parts[0].trim();
                    String task = parts[1].trim();
                    data.put(empId, task);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
            e.printStackTrace();
        }
        return data;
    }
}