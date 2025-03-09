package com.sa.osgi.weightsensorservice;

import java.util.Map;

public interface IPackageWeightSensor {
    double getWeight(String packageId);
    void recordWeight(String packageId, double weight);
    Map<String, Double> getAllWeights(); 
}