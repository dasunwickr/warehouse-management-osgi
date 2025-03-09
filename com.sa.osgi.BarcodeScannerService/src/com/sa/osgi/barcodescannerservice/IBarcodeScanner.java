package com.sa.osgi.barcodescannerservice;

import java.util.Map;

public interface IBarcodeScanner {
    String scanPackage(String packageId);
    void addPackage(String packageId, String productName);
    Map<String, String> getAllPackages(); 
}