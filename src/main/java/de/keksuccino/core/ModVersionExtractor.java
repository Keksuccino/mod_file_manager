package de.keksuccino.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class ModVersionExtractor {

    private static final String GRADLE_PROPERTIES_PATH = new File("..", "gradle.properties").getAbsolutePath();

    /**
     * Reads the gradle.properties file and returns the value of the "mod_version" property.
     */
    public static String getModVersion() {
        try {
            Properties props = new Properties();
            try (InputStream in = new FileInputStream(GRADLE_PROPERTIES_PATH)) {
                props.load(in);
            }
            return Objects.requireNonNullElse(props.getProperty("mod_version"), "0.0.0");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "0.0.0";
    }

    /**
     * Reads the gradle.properties file and returns the value for the given key.
     *
     * @param key the property key.
     * @return the corresponding property value, or null if not found.
     * @throws IOException if the file cannot be read.
     */
    public static String getProperty(String key) throws IOException {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(GRADLE_PROPERTIES_PATH)) {
            props.load(in);
        }
        return props.getProperty(key);
    }

}
