package com.vsu.wsd;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.vsu.common.net.http.HttpUtil;
import com.vsu.common.net.http.StaticFileHandler;
import com.vsu.wsd.sensor.RfParser;
import com.vsu.wsd.sensor.RfData;
import com.vsu.wsd.sensor.Constants;
import com.vsu.wsd.sensor.Sensor;
import com.vsu.wsd.sensor.SensorFactory;
import com.vsu.wsd.sensor.SerialListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Adapted from https://github.com/netty/netty/tree/netty-4.0.10.Final/example/src/main/java/io/netty/example/http/websocketx/server
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 * @author Victor Su
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);
    private static final String WEBSOCKET_PATH = "/ws";

    private final Settings settings;

    private WebSocketServerHandshaker handshaker;
    private StaticFileHandler staticFileHandler;
    private Sensor sensor;
    private int listenerId;
    private ChannelHandlerContext context;

    private static List<Integer> temperatureSamples = new ArrayList<Integer>();
    private static List<Integer> humiditySamples = new ArrayList<Integer>();

    private static Map<Long, Integer[]> sensorHistory = new HashMap<Long, Integer[]>();

    public HttpServerHandler(final Settings settings) {
        this.settings = settings;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handleHttpRequest(final ChannelHandlerContext ctx, final FullHttpRequest req) throws Exception {
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess()) {
            HttpUtil.sendHttpResponse(ctx, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        final String uri = req.getUri();

        if (uri.startsWith(WEBSOCKET_PATH)) {
            // Web socket handshake
            final WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req), null, false);

            handshaker = wsFactory.newHandshaker(req);

            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), req);
            }

            if (sensor == null) {
                sensor = SensorFactory.get();
                listenerId = sensor.addListener(new WsSerialListener());
            }

        } else {
            // Static file request
            if (staticFileHandler == null) {
                staticFileHandler = new StaticFileHandler();
            }

            staticFileHandler.handleHttpRequest(ctx, req);
        }
    }

    private void handleWebSocketFrame(final ChannelHandlerContext ctx, final WebSocketFrame frame) {
        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            sensor.removeListener(listenerId);

        } else if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));

        } else if (frame instanceof PongWebSocketFrame) {
            // nothing to do

        } else if (frame instanceof TextWebSocketFrame) {
            final String request = ((TextWebSocketFrame) frame).text();

            //logger.info("request: " + request);

            try {
                JSONObject requestJson = new JSONObject(request);

                if (requestJson.has(Constants.KEY_OP)) {
                    String op = requestJson.getString(Constants.KEY_OP);

                    Map<String, Object> data = new HashMap<String, Object>();

                    if (op.equals(Constants.OP_QUERY)) {
                        if (requestJson.has(Constants.KEY_TYPE)) {
                            String field = requestJson.getString(Constants.KEY_TYPE);

                            if (field.equals(Constants.TYPE_TEMPERATURE) || field.equals(Constants.TYPE_HUMIDITY)) {
                                context = ctx;
                                sensor.getRFData(listenerId);
                            }

                            if (field.equals(Constants.TYPE_SENSOR0)) {
                                context = ctx;
                                sensor.getSensorData(listenerId, 0);
                            }

                            if (field.equals(Constants.TYPE_SENSOR1)) {
                                context = ctx;
                                sensor.getSensorData(listenerId, 1);
                            }

                            if (field.equals(Constants.TYPE_SENSOR2)) {
                                context = ctx;
                                sensor.getSensorData(listenerId, 2);
                            }

                            if (field.equals(Constants.TYPE_SENSOR3)) {
                                context = ctx;
                                sensor.getSensorData(listenerId, 3);
                            }

                            if (field.equals(Constants.TYPE_SENSOR4)) {
                                context = ctx;
                                sensor.getSensorData(listenerId, 4);
                            }

                            if (field.equals(Constants.TYPE_HISTORY)) {
                                try {
                                    JSONObject json = new JSONObject();

                                    JSONArray array = new JSONArray();

                                    Iterator it = sensorHistory.entrySet().iterator();
                                    while (it.hasNext()) {
                                        JSONObject item = new JSONObject();

                                        Map.Entry pairs = (Map.Entry) it.next();

                                        item.put(Constants.KEY_DATETIME, pairs.getKey());
                                        item.put(Constants.TYPE_TEMPERATURE, ((Integer[]) pairs.getValue())[0]);
                                        item.put(Constants.TYPE_HUMIDITY, ((Integer[]) pairs.getValue())[1]);

                                        array.put(item);
                                    }

                                    json.put(Constants.KEY_RESULT, Constants.RESULT_OK);
                                    json.put(Constants.TYPE_HISTORY, array);

                                    ctx.channel().writeAndFlush(new TextWebSocketFrame(json.toString()));

                                } catch (JSONException e) {

                                }
                            }
                        } else {
                            data.put(Constants.KEY_RESULT, Constants.RESULT_ERROR);
                            data.put(Constants.KEY_MESSAGE, "No type");

                            sendJsonResponse(ctx, data);
                        }

                    } else if (op.equals(Constants.OP_RESET)) {
                        context = ctx;
                        sensor.reset(listenerId);

                    } else {
                        data.put(Constants.KEY_RESULT, Constants.RESULT_ERROR);
                        data.put(Constants.KEY_MESSAGE, "Unknown operation");

                        sendJsonResponse(ctx, data);
                    }
                }

            }
            catch (Exception e) {
                Map<String, Object> data = new HashMap<String, Object>();

                data.put(Constants.KEY_RESULT, Constants.RESULT_ERROR);
                data.put(Constants.KEY_MESSAGE, e.getMessage());

                sendJsonResponse(ctx, data);
            }

        } else {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                    .getName()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //logger.info("exceptionCaught: ", cause);
        //cause.printStackTrace();
        ctx.close();
    }

    private String getWebSocketLocation(FullHttpRequest req) {
        return "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
    }

    private void sendJsonResponse(final ChannelHandlerContext ctx, final Map<String, Object> map) {
        try {
            JSONObject json = new JSONObject();

            for (Map.Entry<String, Object> item : map.entrySet()) {
                json.put(item.getKey(), item.getValue());
            }

            //logger.info("write: " + json.toString());

            ctx.channel().writeAndFlush(new TextWebSocketFrame(json.toString()));

        } catch (JSONException e) {

        }
    }

    /**
     * Returns the majority value from a set of samples.
     * @param samples  The sample set
     * @return         The majority value or Integer.MIN_VALUE if none
     */
    public static int getMajorityValue(List<Integer> samples) {
        int val = Integer.MIN_VALUE;

        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i : samples) {
            Integer count = map.get(i);
            map.put(i, count != null ? count + 1 : 0);
        }

        Map.Entry<Integer, Integer> majority = Collections.max(map.entrySet(),
                new Comparator<Map.Entry<Integer, Integer>>() {
                    @Override
                    public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                });

        if (majority.getValue() >= 2) {
            val = majority.getKey();
        }

        return val;
    }

    private class WsSerialListener implements SerialListener {
        public void onDataReceived(String data) {

            //logger.info("received: " + data);

            if (data.startsWith(Constants.RESP_ACK)) {
                Map<String, Object> map = new HashMap<String, Object>();

                map.put(Constants.KEY_RESULT, Constants.RESULT_OK);
                map.put(Constants.KEY_DATETIME, System.currentTimeMillis());

                // get the resource associated with the request code and send response
                sendJsonResponse(context, map);

            } else if (data.startsWith(Constants.RESP_NAK)) {
                Map<String, Object> map = new HashMap<String, Object>();

                map.put(Constants.KEY_RESULT, Constants.RESULT_ERROR);
                map.put(Constants.KEY_DATETIME, System.currentTimeMillis());

                // get the resource associated with the request code and send response
                sendJsonResponse(context, map);

            } else if (data.startsWith(Constants.RESP_RF)) {
                // remove the packet header before parsing
                RfData rfData = RfParser.parse(data.substring(3));

                Map<String, Object> map = new HashMap<String, Object>();

                if (rfData.temperature != Integer.MIN_VALUE) {
                    map.put(Constants.TYPE_TEMPERATURE, rfData.temperature);
                }

                if (rfData.humidity != Integer.MIN_VALUE) {
                    map.put(Constants.TYPE_HUMIDITY, rfData.humidity);
                }

                if (!map.isEmpty()) {
                    map.put(Constants.KEY_RESULT, Constants.RESULT_OK);
                } else {
                    map.put(Constants.KEY_RESULT, Constants.RESULT_ERROR);
                }

                map.put(Constants.KEY_DATETIME, System.currentTimeMillis());
                sendJsonResponse(context, map);

            } else if (data.startsWith(Constants.RESP_RF_PUSH)) {
                // remove the packet header before parsing
                RfData rfData = RfParser.parse(data.substring(3));

                if (rfData.sequence == RfData.SEQ_START) {
                    temperatureSamples.clear();
                    humiditySamples.clear();
                }

                if (rfData.temperature != Integer.MIN_VALUE) {
                    temperatureSamples.add(rfData.temperature);
                }

                if (rfData.humidity != Integer.MIN_VALUE) {
                    humiditySamples.add(rfData.humidity);
                }

                if (rfData.sequence == RfData.SEQ_END) {
                    Map<String, Object> map = new HashMap<String, Object>();

                    int validatedTemperature = getMajorityValue(temperatureSamples);

                    if (validatedTemperature != Integer.MIN_VALUE) {
                        map.put(Constants.TYPE_TEMPERATURE, validatedTemperature);
                    }

                    int validatedHumidity = getMajorityValue(humiditySamples);

                    if (validatedHumidity != Integer.MIN_VALUE) {
                        map.put(Constants.TYPE_HUMIDITY, validatedHumidity);
                    }

                    if (!map.isEmpty()) {
                        long timeStamp = System.currentTimeMillis();

                        map.put(Constants.KEY_RESULT, Constants.RESULT_OK);
                        map.put(Constants.KEY_DATETIME, timeStamp);

                        if (context != null) {
                            sendJsonResponse(context, map);
                        }

                        sensorHistory.put(timeStamp, new Integer[] { validatedTemperature, validatedHumidity });

                        // remove history older than one day
                        final long oneDayAgo = timeStamp - 86400000;

                        Predicate<Long> dayFilter = new Predicate<Long>() {
                            public boolean apply(Long timeStamp) {
                                return (timeStamp >= oneDayAgo);
                            }
                        };

                        sensorHistory = Maps.filterKeys(sensorHistory, dayFilter);
                    }
                }

            } else if (data.startsWith(Constants.RESP_SENSOR)) {
                if (data.length() >= 9) {
                    Map<String, Object> map = new HashMap<String, Object>();

                    map.put(Constants.KEY_RESULT, Constants.RESULT_OK);
                    map.put(Constants.KEY_DATETIME, System.currentTimeMillis());

                    int channel = Integer.parseInt(data.substring(3, 5), 16);
                    int value = Integer.parseInt(data.substring(5, 9), 16);

                    switch (channel) {
                        case 0:
                            map.put(Constants.TYPE_SENSOR0, value);
                            break;

                        case 1:
                            map.put(Constants.TYPE_SENSOR1, value);
                            break;

                        case 2:
                            map.put(Constants.TYPE_SENSOR2, value);
                            break;

                        case 3:
                            map.put(Constants.TYPE_SENSOR3, value);
                            break;

                        case 4:
                            map.put(Constants.TYPE_SENSOR4, value);
                            break;
                    }

                    sendJsonResponse(context, map);
                }
            }
        }
    }

}
