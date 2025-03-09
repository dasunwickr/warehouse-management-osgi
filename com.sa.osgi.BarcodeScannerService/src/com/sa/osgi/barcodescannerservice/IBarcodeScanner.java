package com.sa.osgi.barcodescannerservice;

public interface IBarcodeScanner {
    String scanPackage(String packageId);
    void addPackage(String id, String product);
}