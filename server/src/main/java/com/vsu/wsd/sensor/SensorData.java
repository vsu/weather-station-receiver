package com.vsu.wsd.sensor;

/**
 * @author Victor Su
 */
public class SensorData {
    private long dateTime;
    private short temperature;
    private byte humidity;

    public SensorData(long dateTime, short temperature, byte humidity) {
        this.dateTime = dateTime;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public long getDateTime() {
        return dateTime;
    }

    public short getTemperature() {
        return temperature;
    }

    public byte getHumidity() {
        return humidity;
    }
}