package com.sa.osgi.orderprocessingservice;

public interface IOrderProcessing {
    double getOrderWeight(String orderId);
    void addOrder(String orderId, double weight);
}