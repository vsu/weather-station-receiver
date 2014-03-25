package com.vsu.wsd;

import com.vsu.common.net.http.HttpServer;
import com.vsu.common.net.ip.IpUtil;
import com.vsu.common.net.ssdp.SsdpServer;
import com.vsu.wsd.sensor.Sensor;
import com.vsu.wsd.sensor.SensorFactory;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Victor Su
 */
public class WSDaemon implements Daemon {
    private final Logger logger = LoggerFactory.getLogger(WSDaemon.class);
    private HttpServer server;

    private final static String SERVICE_NAME = "wsd";
    private SsdpServer ssdp;
    private Sensor sensor;

    @Override
    public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
        String[] args = daemonContext.getArguments();

        final String settingsFile;
        final String logFile;

        if (args.length > 1) {
            settingsFile = args[0];
            logFile = args[1];

        } else if (args.length > 0) {
            settingsFile = args[0];
            logFile = "config/log4j.properties";

        } else {
            settingsFile = "config/server.yaml";
            logFile = "config/log4j.properties";

        }

        PropertyConfigurator.configure(logFile);

        Settings settings = Settings.read(settingsFile);
        if (settings != null) {
            logger.info("Configuring server from {}", settingsFile);

            if (settings.host == null) {
                settings.host = IpUtil.detectIpAddress();
            }

            if (settings.host != null) {
                server = new HttpServer(settings, new HttpServerInitializer(settings));
                ssdp = new SsdpServer(settings.host, settings.port, SERVICE_NAME);

                SensorFactory.setDefaultPortName(settings.portName);
                sensor = SensorFactory.get();
            } else {
                logger.error("Could not set IP address for server.");
            }

        } else {
            logger.error("Configuration file at {} could not be found or parsed properly.", settingsFile);
        }
    }

    @Override
    public void start() throws Exception {
        sensor.connect();
        server.start();
        ssdp.start();
    }

    @Override
    public void stop() throws Exception {
        ssdp.shutdown();
        server.shutdown();
        sensor.close();

        try {
            ssdp.join();
        } catch(InterruptedException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }

    @Override
    public void destroy() {
        ssdp = null;
    }
}
