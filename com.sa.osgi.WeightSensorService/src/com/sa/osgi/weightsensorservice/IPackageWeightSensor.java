package com.sa.osgi.weightsensorservice;

public interface IPackageWeightSensor {
    double getWeight(String packageId);
    void recordWeight(String packageId, double weight);
}