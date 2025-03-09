package com.sa.osgi.inventory;

import com.sa.osgi.barcodescannerservice.IBarcodeScanner;
import com.sa.osgi.orderprocessingservice.IOrderProcessing;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.Scanner;

public class Activator implements BundleActivator {

    private ServiceReference<IBarcodeScanner> barcodeScannerRef;
    private ServiceReference<IOrderProcessing> orderProcessingRef;

    @Override
    public void start(BundleContext context) throws Exception {
        barcodeScannerRef = context.getServiceReference(IBarcodeScanner.class);
        orderProcessingRef = context.getServiceReference(IOrderProcessing.class);

        if (barcodeScannerRef != null && orderProcessingRef != null) {
            IBarcodeScanner barcodeScanner = context.getService(barcodeScannerRef);
            IOrderProcessing orderProcessing = context.getService(orderProcessingRef);

            Scanner scanner = new Scanner(System.in);
            System.out.println("[Inventory Manager] Starting interactive mode. Type 'exit' to quit.");

            while (true) {
                System.out.print("[Inventory Manager] Enter package ID: ");
                String packageId = scanner.nextLine();
                if (packageId.equalsIgnoreCase("exit")) break;

                String product = barcodeScanner.scanPackage(packageId);
                System.out.println("📦 Product scanned: " + product);

                System.out.print("[Inventory Manager] Enter order ID: ");
                String orderId = scanner.nextLine();
                if (orderId.equalsIgnoreCase("exit")) break;

                double weight = orderProcessing.getOrderWeight(orderId);
                System.out.println("📦 Order weight: " + weight);
            }

            System.out.println("[Inventory Manager] Interactive mode stopped.");
        } else {
            System.out.println("Required services are not available.");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (barcodeScannerRef != null) {
            context.ungetService(barcodeScannerRef);
        }
        if (orderProcessingRef != null) {
            context.ungetService(orderProcessingRef);
        }

        System.out.println("Inventory Manager Service stopped.");
    }
}