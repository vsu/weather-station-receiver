package com.vsu.wsd.sensor;

/**
 * @author Victor Su
 */
public class RfData {
    public final static int SEQ_START = 1;
    public final static int SEQ_END = 3;

    public int sequence;
    public int temperature;
    public int humidity;


    public RfData() {
        sequence = Integer.MIN_VALUE;
        temperature = Integer.MIN_VALUE;
        humidity = Integer.MIN_VALUE;
    }
}