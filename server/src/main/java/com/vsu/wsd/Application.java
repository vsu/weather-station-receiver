package com.vsu.wsd;

import com.vsu.common.net.http.HttpServer;
import com.vsu.common.net.ip.IpUtil;
import com.vsu.wsd.sensor.Sensor;
import com.vsu.wsd.sensor.SensorFactory;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Console application for development testing.
 *
 * @author Victor Su
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(final String[] args) {

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

        final Settings settings = Settings.read(settingsFile);
        if (settings != null) {
            logger.info("Configuring server from {}", settingsFile);

            try {
                if (settings.host == null) {
                    settings.host = IpUtil.detectIpAddress();
                }

                if (settings.host != null) {
                    SensorFactory.setDefaultPortName(settings.portName);
                    Sensor sensor = SensorFactory.get();
                    sensor.connect();

                    final HttpServer server = new HttpServer(settings, new HttpServerInitializer(settings));
                    server.start();

                    int c;
                    do {
                        c = System.in.read();
                    }
                    while (c > -1 && c != 'x');

                    server.shutdown();
                    server.join();

                    sensor.close();
                } else {
                    logger.error("Could not set IP address for server.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            logger.error("Configuration file at {} could not be found or parsed properly.", settingsFile);
        }
    }

}