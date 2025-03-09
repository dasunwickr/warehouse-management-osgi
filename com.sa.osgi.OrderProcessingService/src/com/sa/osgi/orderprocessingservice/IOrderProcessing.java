package com.sa.osgi.orderprocessingservice;

import java.util.Map;

public interface IOrderProcessing {
    double getOrderAmount(String orderId);
    void addOrder(String orderId, double amount);
    Map<String, Double> getAllOrders();
}