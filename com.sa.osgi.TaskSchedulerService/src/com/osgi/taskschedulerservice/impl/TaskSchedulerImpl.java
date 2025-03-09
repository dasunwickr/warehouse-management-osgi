package com.osgi.taskschedulerservice.impl;


import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.osgi.taskschedulerservice.ITaskScheduler;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

public class TaskSchedulerImpl implements ITaskScheduler {

    private static final String DATA_DIR = "D:/projects/sliit/y3s2/sa/warehouse-management-osgi/data";
    private static final String DATA_FILE = DATA_DIR + "/tasks.txt";

    private Map<String, String> tasks = new HashMap<>();
    private ServiceRegistration<ITaskScheduler> registration;

    private static final Logger LOGGER = Logger.getLogger(TaskSchedulerImpl.class.getName());

    public void start(BundleContext context) {
        // Ensure the 'data' directory exists
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            boolean success = dataDir.mkdirs();
            if (!success) {
                LOGGER.severe("Failed to create directory: " + dataDir.getAbsolutePath());
                return;
            }
        }
        LOGGER.info("Data directory created: " + dataDir.exists());

        loadTasks();
        registration = context.registerService(ITaskScheduler.class, this, null);
        LOGGER.info("Employee Task Scheduler Service started.");

        // Start the scanner in the main thread
        startScanner();
    }

    public void stop() {
        saveTasks();
        registration.unregister();
        LOGGER.info("Employee Task Scheduler Service stopped.");
    }

    @Override
    public void assignTask(String employeeId, String taskDetails) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            LOGGER.warning("Employee ID cannot be empty.");
            return;
        }
        if (taskDetails == null || taskDetails.trim().isEmpty()) {
            LOGGER.warning("Task details cannot be empty.");
            return;
        }

        tasks.put(employeeId, taskDetails);
        saveTasks();
        System.out.println("📋 Task assigned to " + employeeId + ": " + taskDetails);
    }

    @Override
    public String getTask(String employeeId) {
        return tasks.get(employeeId);
    }

    private void loadTasks() {
        File file = new File(DATA_FILE);

        // If the file doesn't exist, create it
        try {
            ensureFileExists(file);
        } catch (IOException e) {
            LOGGER.severe("Error creating file: " + file.getAbsolutePath());
            return;
        }

        // Load data from the file
        try {
            loadFromFile(file);
        } catch (IOException e) {
            LOGGER.severe("Error loading data from file: " + file.getAbsolutePath());
        }
    }

    private void saveTasks() {
        File file = new File(DATA_FILE);

        // Save data to the file
        try {
            saveToFile(file);
        } catch (IOException e) {
            LOGGER.severe("Error saving data to file: " + file.getAbsolutePath());
        }
    }

    private void startScanner() {
        Scanner scanner = new Scanner(System.in); // Do NOT close this scanner
        System.out.println("[Task Scheduler] Starting interactive mode. Type 'exit' to quit.");

        while (true) {
            System.out.print("[Task Scheduler] Enter employee ID: ");
            String empId = scanner.nextLine();
            if (empId.equalsIgnoreCase("exit")) break;

            System.out.print("[Task Scheduler] Enter task: ");
            String task = scanner.nextLine();
            assignTask(empId, task);
        }

        System.out.println("[Task Scheduler] Interactive mode stopped.");
        // DO NOT CLOSE THE SCANNER HERE
    }

    private void ensureFileExists(File file) throws IOException {
        if (!file.exists()) {
            boolean success = file.createNewFile();
            if (!success) {
                throw new IOException("Failed to create file: " + file.getAbsolutePath());
            }
        }
    }

    private void loadFromFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    tasks.put(parts[0], parts[1]);
                }
            }
        }
    }

    private void saveToFile(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, String> entry : tasks.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        }
    }
}