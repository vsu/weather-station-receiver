package com.vsu.wsd;

import com.vsu.common.net.http.HttpUtil;
import com.vsu.common.net.http.StaticFileHandler;
import com.vsu.wsd.sensor.Constants;
import com.vsu.wsd.sensor.Sensor;
import com.vsu.wsd.sensor.SensorData;
import com.vsu.wsd.sensor.SensorFactory;
import com.vsu.wsd.sensor.SensorListener;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

import io.netty.handler.codec.http.websocketx.*;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final Settings settings;

    private WebSocketServerHandshaker handshaker;
    private StaticFileHandler staticFileHandler;
    private Sensor sensor;
    private int listenerId;
    private ChannelHandlerContext context;

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

        if (uri.startsWith(Constants.WEBSOCKET_PATH)) {
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
                listenerId = sensor.addListener(new WsSensorListener());
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

                            if (field.equals(Constants.TYPE_CHANNEL0)) {
                                context = ctx;
                                sensor.getChannelData(listenerId, 0);
                            }

                            if (field.equals(Constants.TYPE_CHANNEL1)) {
                                context = ctx;
                                sensor.getChannelData(listenerId, 1);
                            }

                            if (field.equals(Constants.TYPE_CHANNEL2)) {
                                context = ctx;
                                sensor.getChannelData(listenerId, 2);
                            }

                            if (field.equals(Constants.TYPE_CHANNEL3)) {
                                context = ctx;
                                sensor.getChannelData(listenerId, 3);
                            }

                            if (field.equals(Constants.TYPE_CHANNEL4)) {
                                context = ctx;
                                sensor.getChannelData(listenerId, 4);
                            }

                            if (field.equals(Constants.TYPE_HISTORY)) {
                                List<SensorData> sensorData = sensor.getSensorData();

                                final ByteBuf dataBytes = Unpooled.directBuffer(
                                        sensorData.size() * ((Long.SIZE + Short.SIZE + Byte.SIZE) / Byte.SIZE));

                                for (SensorData item : sensorData) {
                                    // byte order is big-endian
                                    dataBytes.writeLong(item.getDateTime());
                                    dataBytes.writeShort(item.getTemperature());
                                    dataBytes.writeByte(item.getHumidity());
                                }

                                ctx.channel().write(new BinaryWebSocketFrame(dataBytes));
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
        return "ws://" + req.headers().get(HOST) + Constants.WEBSOCKET_PATH;
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

    private class WsSensorListener implements SensorListener {
        @Override
        public void onResponseReceived(Map<String, Object> response) {
            if (context != null) {
                sendJsonResponse(context, response);
            }
        }
    }

}
