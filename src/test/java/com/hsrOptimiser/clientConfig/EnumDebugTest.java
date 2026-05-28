package com.hsrOptimiser.clientConfig;

// File: EnumDebugTest.java
public class EnumDebugTest {
    public static void main(String[] args) {
        System.out.println("🔍 Starting enum load test...");

        try {
            // Force class loading by referencing a constant
            Class<?> enumClass = Class.forName("com.hsrOptimiser.clientConfig.AsagiLightConeMetadata");
            System.out.println("✅ Class loaded: " + enumClass.getName());

            // Try to access values
            Object[] constants = enumClass.getEnumConstants();
            System.out.println("✅ Enum constants count: " + constants.length);

            // Try to call a static method if it exists
            java.lang.reflect.Method getInfoById = enumClass.getMethod("getInfoById", String.class);
            Object result = getInfoById.invoke(null, "20000");
            System.out.println("✅ getInfoById('20000') returned: " + result);

        } catch (ExceptionInInitializerError e) {
            System.err.println("❌ ExceptionInInitializerError caught!");
            System.err.println("Message: " + e.getMessage());
            Throwable cause = e.getCause();
            if (cause != null) {
                System.err.println("🔥 ROOT CAUSE: " + cause.getClass().getName());
                System.err.println("🔥 ROOT MESSAGE: " + cause.getMessage());
                cause.printStackTrace(); // ← THIS IS WHAT YOU NEED
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Class not found - check package name!");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Other exception: " + e.getClass().getName());
            e.printStackTrace();
        }
    }
}