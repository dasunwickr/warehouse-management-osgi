package com.osgi.taskschedulerservice;

import java.util.Map;

public interface ITaskScheduler {;
	String getTasksForEmployee(String empId);
	Map<String, String> getAllTasks();
	void assignTask(String empId, String task);
}