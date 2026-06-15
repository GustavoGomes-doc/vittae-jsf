package com.vittae.util;

import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {
    private static final Properties props = new Properties();
    
    static {
        try (InputStream is = ConfigUtil.class
                .getResourceAsStream("/config.properties")) {
            props.load(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String get(String key) {
        return props.getProperty(key);
    }
}