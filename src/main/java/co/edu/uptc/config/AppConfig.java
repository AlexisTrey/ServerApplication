package co.edu.uptc.config;

import co.edu.uptc.util.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties props = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream in = AppConfig.class
                .getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) props.load(in);
        } catch (IOException e) {
            System.err.println("Could not load config.properties, using defaults.");
        }
    }

    public static int getPort() {
        return Integer.parseInt(
                props.getProperty("server.port",
                        String.valueOf(Utilities.DEFAULT_PORT)));
    }

    public static int getSpeed() {
        return Integer.parseInt(
                props.getProperty("server.speed",
                        String.valueOf(Utilities.DEFAULT_SPEED_MS)));
    }

    private AppConfig() {}
}
