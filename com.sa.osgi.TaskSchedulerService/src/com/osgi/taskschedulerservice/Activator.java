package com.osgi.taskschedulerservice;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.osgi.taskschedulerservice.impl.TaskSchedulerImpl;

public class Activator implements BundleActivator {

    private TaskSchedulerImpl taskScheduler;

    @Override
    public void start(BundleContext context) throws Exception {
        taskScheduler = new TaskSchedulerImpl();
        taskScheduler.start(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        taskScheduler.stop();
    }
}