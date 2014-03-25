package com.vsu.wsd.sensor;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
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
    private Map<Integer, SerialListener> listeners = new HashMap<Integer, SerialListener>();

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
                logger.info("close: " + e.getMessage());
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

    public void getSensorData(int id, int channel) {
        sendSerial(id, Constants.REQ_SENSOR, String.format("%02x", (0xFF & channel)).toUpperCase(Locale.US));
    }

    public synchronized int addListener(SerialListener listener) {
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
                        synchronized(listeners) {
                            if (listeners.size() > 0) {
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
                                        // examine the packet id to determine which listener to call
                                        String idStr = line.substring(1, 3);
                                        int id = Integer.parseInt(idStr, 16);

                                        // packet id 0 indicates broadcast
                                        if (id == 0) {
                                            for (SerialListener listener : listeners.values()) {
                                                listener.onDataReceived(line);
                                            }
                                        } else {
                                           if (listeners.containsKey(id)) {
                                               listeners.get(id).onDataReceived(line);
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

    private int bytesToInt(byte[] b) {
        return (b[0] & 0xFF) + ((b[1] & 0xFF) * 256);
    }

    private void delayMicroseconds(int microseconds) {
        long end = System.nanoTime() + (microseconds * 1000);
        long now;

        do {
            now = System.nanoTime();
        } while (end >= now);
    }
}
