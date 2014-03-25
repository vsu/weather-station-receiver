package com.vsu.wsd.sensor;

/**
 * @author Victor Su
 */
public class Constants {
    public final static String WS_PATH = "/ws";

    public final static String KEY_DATETIME= "dateTime";
    public final static String KEY_MESSAGE = "message";
    public final static String KEY_OP = "op";
    public final static String KEY_RESULT = "result";
    public final static String KEY_TYPE = "type";

    public final static String TYPE_HUMIDITY = "humidity";
    public final static String TYPE_SENSOR0 = "sensor0";
    public final static String TYPE_SENSOR1 = "sensor1";
    public final static String TYPE_SENSOR2 = "sensor2";
    public final static String TYPE_SENSOR3 = "sensor3";
    public final static String TYPE_SENSOR4 = "sensor4";
    public final static String TYPE_TEMPERATURE = "temperature";

    public final static String OP_QUERY = "query";
    public final static String OP_RESET = "reset";

    public final static String RESULT_ERROR = "error";
    public final static String RESULT_OK = "ok";

    public final static String REQ_RESET = "R";
    public final static String REQ_RF = "F";
    public final static String REQ_SENSOR = "S";

    public final static String RESP_RF_PUSH = "P";
    public final static String RESP_RF = "F";
    public final static String RESP_SENSOR = "S";
    public final static String RESP_ACK = "K";
    public final static String RESP_NAK = "X";

    public final static String CH_TERM = "\r";
}
