package com.vsu.wsd.sensor;

/**
 * @author Victor Su
 */
public class Constants {
    public final static String WEBSOCKET_PATH = "/ws";

    public final static String KEY_DATETIME= "dateTime";
    public final static String KEY_MESSAGE = "message";
    public final static String KEY_OP = "op";
    public final static String KEY_RESULT = "result";
    public final static String KEY_TYPE = "type";

    public final static String TYPE_HISTORY = "history";
    public final static String TYPE_HUMIDITY = "humidity";
    public final static String TYPE_CHANNEL0 = "channel0";
    public final static String TYPE_CHANNEL1 = "channel1";
    public final static String TYPE_CHANNEL2 = "channel2";
    public final static String TYPE_CHANNEL3 = "channel3";
    public final static String TYPE_CHANNEL4 = "channel4";
    public final static String TYPE_TEMPERATURE = "temperature";

    public final static String OP_QUERY = "query";
    public final static String OP_RESET = "reset";

    public final static String RESULT_ERROR = "error";
    public final static String RESULT_OK = "ok";

    public final static String REQ_RESET = "R";
    public final static String REQ_RF = "F";
    public final static String REQ_CHANNEL = "C";

    public final static String RESP_RF_PUSH = "P";
    public final static String RESP_RF = "F";
    public final static String RESP_CHANNEL = "C";
    public final static String RESP_ACK = "K";
    public final static String RESP_NAK = "X";

    public final static String CH_TERM = "\r";
}
