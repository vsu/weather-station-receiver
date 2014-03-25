package com.vsu.wsd;

import com.vsu.common.net.http.HttpSettings;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author Victor Su
 */
public class Settings extends HttpSettings {

    /**
     * COM port name.
     */
    public String portName;

    public static Settings read(final String file) {
        try {
            final InputStream input = new FileInputStream(new File(file));
            return read(input);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Settings read(final InputStream stream) {
        if (stream == null) {
            return null;
        }

        try {
            final Constructor constructor = new Constructor(Settings.class);
            final TypeDescription settingsDescription = new TypeDescription(Settings.class);
            constructor.addTypeDescription(settingsDescription);

            final Yaml yaml = new Yaml(constructor);

            return yaml.loadAs(stream, Settings.class);
        } catch (Exception e) {
            return null;
        }
    }

}
