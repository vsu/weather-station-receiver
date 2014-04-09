package com.vsu.wsd.sensor;

import java.util.Map;

/**
 * @author Victor Su
 */
public interface SensorListener {
    public void onResponseReceived(int listenerId, Map<String, Object> response);
}
