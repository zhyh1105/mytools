package com.puhui.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author lixin
 */
public class PropertiesUtil {
    private static final ConcurrentHashMap<String, Properties> PROPS_HOLDER = new ConcurrentHashMap<>();
    private static final String PRE_PROPS = "/com/puhui/util/mail/props/";
    private static final String SUF_PROPS = ".properties";
    private static final Logger log = Logger.getLogger(PropertiesUtil.class);

    /**
     * @param file
     * @return
     */
    public static Properties readPropertiesFile(String file) {
        InputStream in = PropertiesUtil.class.getResourceAsStream(file);
        Properties prop = new Properties();
        try {
            prop.load(in);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return prop;
    }

    public static Properties getPropsByEmail(String email) {
        String key = parseEmail(email);
        if (PROPS_HOLDER.get(key) != null) {
            return PROPS_HOLDER.get(key);
        }
        InputStream in = PropertiesUtil.class.getResourceAsStream(PRE_PROPS + key + SUF_PROPS);
        Properties prop = new Properties();
        try {
            prop.load(in);
            PROPS_HOLDER.putIfAbsent(key, prop);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return prop;
    }

    public static String parseEmail(String email) {
        if (StringUtils.isNotBlank(email)) {
            String[] arr = email.split("@");
            return arr[arr.length - 1];
        }
        return null;
    }

    private static final Properties DEFAULT_PROPS = readPropertiesFile("/client_spconf.properties");

    public static String getProps(String key) {
        return DEFAULT_PROPS.getProperty(key);
    }

    public static void main(String[] args) {
        System.out.println(getProps("mail.subject"));
    }
}
