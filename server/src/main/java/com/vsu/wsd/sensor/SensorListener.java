package com.vsu.wsd.sensor;

import java.util.Map;

/**
 * @author Victor Su
 */
public interface SensorListener {
    public void onResponseReceived(Map<String, Object> response);
}
