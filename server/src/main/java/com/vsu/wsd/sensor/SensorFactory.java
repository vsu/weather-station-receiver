package com.vsu.wsd.sensor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Victor Su
 */
public final class SensorFactory {
    private static String defaultPortName = "";
    private static Map<String, Sensor> store = new HashMap<String, Sensor>();

    public static Sensor get(String portName) {
        synchronized (store) {
            Sensor result = store.get(portName);

            if (result == null) {
                result = new Sensor(portName);
                store.put(portName, result);
            }

            return result;
        }
    }

    public static Sensor get() {
        return !defaultPortName.isEmpty() ? get(defaultPortName) : null;
    }

    public static void setDefaultPortName(String portName) {
        synchronized(defaultPortName) {
            System.out.println(portName);
            defaultPortName = portName;
        }
    }
}
