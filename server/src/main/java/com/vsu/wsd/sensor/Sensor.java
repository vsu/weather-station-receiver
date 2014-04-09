package com.vsu.wsd.sensor;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author Victor Su
 */
public class Sensor {
    private final Logger logger = LoggerFactory.getLogger(Sensor.class);

    private String portName;
    private SerialPort serialPort = null;
    private SerialReader reader = null;
    private Map<Integer, SensorListener> listeners = new HashMap<Integer, SensorListener>();

    private static List<Integer> temperatureSamples = new ArrayList<Integer>();
    private static List<Integer> humiditySamples = new ArrayList<Integer>();

    private static List<SensorData> sensorData = new ArrayList<SensorData>();

    public Sensor(final String portName) {
        this.portName = portName;
    }

    public void close() {
        logger.info("close: " + portName);

        synchronized(listeners) {
            listeners.clear();
        }

        if (reader != null) {
            reader.shutdown();

            try {
                reader.join();
            } catch(InterruptedException e) {
                logger.debug("close: " + e.getMessage());
            }

            reader = null;
        }

        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
    }

    public void reset(int id) {
        sendSerial(id, Constants.REQ_RESET, null);
    }

    public void getRFData(int id) {
        sendSerial(id, Constants.REQ_RF, null);
    }

    public void getChannelData(int id, int channel) {
        sendSerial(id, Constants.REQ_CHANNEL, String.format("%02x", (0xFF & channel)).toUpperCase(Locale.US));
    }

    public List<SensorData> getSensorData() {
        return sensorData;
    }

    public synchronized int addListener(SensorListener listener) {
        // get the first unused id, which is a non-zero unsigned byte value
        Set<Integer> keys = listeners.keySet();

        int i;
        for (i = 1; i < 256; i++) {
            if (!keys.contains(i)) {
                break;
            }
        }

        listeners.put(i, listener);
        return i;
    }

    public synchronized void removeListener(int id) {
        if (listeners.containsKey(id)) {
            listeners.remove(id);
        }
    }

    public void connect() throws Exception {
        logger.info("connect: " + portName);

        serialPort = null;

        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

        if (portIdentifier.isCurrentlyOwned()) {
            logger.info("connect: port " + portName + " is currently in use.");
        } else {
            int timeout = 2000;
            CommPort commPort = portIdentifier.open(this.getClass().getName(), timeout);

            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort)commPort;

                serialPort.setSerialPortParams(
                        9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

                InputStream in = serialPort.getInputStream();

                reader = new SerialReader(in);
                reader.start();

            } else {
                logger.info("connect: only serial ports are supported.");
            }
        }
    }

    class SerialReader extends Thread {
        private InputStream in;
        private StringBuilder builder;
        private boolean running = false;

        public SerialReader(InputStream in) {
            this.in = in;
            this.builder = new StringBuilder();
        }

        @Override
        public void start() {
            running = true;
            super.start();
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int len = -1;
            try {
                while (running && (len = this.in.read(buffer)) > -1) {
                    builder.append(new String(buffer, 0, len));
                    if (builder.indexOf(Constants.CH_TERM) != -1)
                    {
                        String str = builder.toString();
                        String response;

                        // if the string is not terminated, then save the incomplete fragment
                        if (!str.endsWith(Constants.CH_TERM)) {
                            int lastPos = str.lastIndexOf(Constants.CH_TERM);

                            if (lastPos != -1) {
                                String fragment = str.substring(lastPos);
                                response = str.substring(0, lastPos);
                                builder = new StringBuilder(fragment);
                            } else {
                                response = str;
                                builder = new StringBuilder();
                            }

                        } else {
                            response = str;
                            builder = new StringBuilder();
                        }

                        String[] lines = response.split(Constants.CH_TERM);
                        for (String line : lines) {
                            if (!line.isEmpty()) {
                                Map<String, Object> map = processReceivedData(line);

                                if (!map.isEmpty()) {
                                    // examine the packet id to determine which listener to call
                                    String idStr = line.substring(1, 3);
                                    int id = Integer.parseInt(idStr, 16);

                                    synchronized(listeners) {
                                        if (listeners.size() > 0) {
                                            // packet id 0 indicates broadcast
                                            if (id == 0) {
                                                for (Map.Entry<Integer, SensorListener> listener: listeners.entrySet()) {
                                                    listener.getValue().onResponseReceived(listener.getKey(), map);
                                                }
                                            } else {
                                                if (listeners.containsKey(id)) {
                                                    listeners.get(id).onResponseReceived(id, map);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void shutdown() {
            running = false;
        }
    }

    private void sendSerial(int id, String command, String arg) {
        if (serialPort != null) {
            try {
                OutputStream out = serialPort.getOutputStream();

                StringBuilder builder = new StringBuilder();
                builder.append(command);
                builder.append(String.format("%02x", (0xFF & id)).toUpperCase(Locale.US));

                if (arg != null) {
                    builder.append(arg);
                }

                builder.append(Constants.CH_TERM);

                out.write(builder.toString().getBytes());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, Object> processReceivedData(String data) {
        logger.debug("processReceivedData: " + data);

        Map<String, Object> map = new HashMap<String, Object>();

        if (data.startsWith(Constants.RESP_ACK)) {
            map.put(Constants.KEY_RESULT, Constants.RESULT_OK);
            map.put(Constants.KEY_DATETIME, System.currentTimeMillis());

            return map;

        } else if (data.startsWith(Constants.RESP_NAK)) {
            map.put(Constants.KEY_RESULT, Constants.RESULT_ERROR);
            map.put(Constants.KEY_DATETIME, System.currentTimeMillis());

            return map;

        } else if (data.startsWith(Constants.RESP_RF)) {
            // remove the packet header before parsing
            RfData rfData = RfParser.parse(data.substring(3));

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

            return map;

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

                    sensorData.add(new SensorData(timeStamp, (short) validatedTemperature, (byte) validatedHumidity));

                    // remove history older than one day
                    final long oneDayAgo = timeStamp - 86400000;

                    Predicate<SensorData> dayFilter = new Predicate<SensorData>() {
                        public boolean apply(SensorData item) {
                            return (item.getDateTime() >= oneDayAgo);
                        }
                    };

                    sensorData = Lists.newArrayList(Iterables.filter(sensorData, dayFilter));

                    logger.debug("sensorData len: " + sensorData.size());

                    return map;
               }
            }

        } else if (data.startsWith(Constants.RESP_CHANNEL)) {
            if (data.length() >= 9) {
                map.put(Constants.KEY_RESULT, Constants.RESULT_OK);
                map.put(Constants.KEY_DATETIME, System.currentTimeMillis());

                int channel = Integer.parseInt(data.substring(3, 5), 16);
                int value = Integer.parseInt(data.substring(5, 9), 16);

                switch (channel) {
                    case 0:
                        map.put(Constants.TYPE_CHANNEL0, value);
                        break;

                    case 1:
                        map.put(Constants.TYPE_CHANNEL1, value);
                        break;

                    case 2:
                        map.put(Constants.TYPE_CHANNEL2, value);
                        break;

                    case 3:
                        map.put(Constants.TYPE_CHANNEL3, value);
                        break;

                    case 4:
                        map.put(Constants.TYPE_CHANNEL4, value);
                        break;
                }

                return map;
            }
        }

        return map;
    }

    /**
     * Returns the majority value from a set of samples.
     * @param samples  The sample set
     * @return         The majority value or Integer.MIN_VALUE if none
     */
    private static int getMajorityValue(List<Integer> samples) {
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
                }
        );

        if (majority.getValue() >= 2) {
            val = majority.getKey();
        }

        return val;
    }
}
