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
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            boolean success = dataDir.mkdirs();
            if (!success) {
                LOGGER.severe("Failed to create directory: " + dataDir.getAbsolutePath());
                return;
            }
        }
        LOGGER.info("Data directory available: " + dataDir.getAbsolutePath());

        loadTasks();
        registration = context.registerService(ITaskScheduler.class, this, null);
        LOGGER.info("Task Scheduler Service started.");

        startScanner();
    }

    public void stop() {
        saveTasks();
        registration.unregister();
        LOGGER.info("Task Scheduler Service stopped.");
    }

    @Override
    public void assignTask(String empId, String task) {
        if (empId == null || empId.trim().isEmpty() || task == null || task.trim().isEmpty()) {
            LOGGER.warning("Invalid employee ID or task description.");
            return;
        }
        tasks.put(empId, task);
        saveTasks();
        System.out.println("📝 Task assigned to Employee ID " + empId + ": " + task);
    }

    private void loadTasks() {
        File file = new File(DATA_FILE);
        try {
            ensureFileExists(file);
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        tasks.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Error loading data from file: " + file.getAbsolutePath());
        }
    }

    private void saveTasks() {
        File file = new File(DATA_FILE);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, String> entry : tasks.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            LOGGER.severe("Error saving data to file: " + file.getAbsolutePath());
        }
    }

    private void startScanner() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("[Task Scheduler] Starting interactive mode. Type 'exit' to quit.");
        while (true) {
            System.out.print("[Task Scheduler] Enter employee ID: ");
            String empId = scanner.nextLine();
            if (empId.equalsIgnoreCase("exit")) break;

            System.out.print("[Task Scheduler] Enter task description: ");
            String task = scanner.nextLine();
            if (task.equalsIgnoreCase("exit")) break;

            assignTask(empId, task);
        }
        System.out.println("[Task Scheduler] Interactive mode stopped.");
    }

    private void ensureFileExists(File file) throws IOException {
        if (!file.exists()) {
            boolean success = file.createNewFile();
            if (!success) {
                throw new IOException("Failed to create file: " + file.getAbsolutePath());
            }
        }
    }
}