package com.vsu.wsd.sensor;

/**
 * @author Victor Su
 */
public class DecodedValue {
    public int sign;
    public int hundreds;
    public int tens;
    public int ones;

    DecodedValue() {
        sign = Integer.MIN_VALUE;
        hundreds = Integer.MIN_VALUE;
        tens = Integer.MIN_VALUE;
        ones = Integer.MIN_VALUE;
    }

    public int getValue() {
        int val = 0;

        if (ones != Integer.MIN_VALUE) {
            val = ones;
        }

        if (tens != Integer.MIN_VALUE) {
            val += 10 * tens;
        }

        if (hundreds != Integer.MIN_VALUE) {
            val += 100 * hundreds;
        }

        if (sign != Integer.MIN_VALUE) {
            val *= sign;
        }

        return val;
    }
}
