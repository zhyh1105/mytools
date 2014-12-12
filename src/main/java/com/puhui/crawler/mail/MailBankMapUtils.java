package com.puhui.crawler.mail;

import java.util.Properties;

import com.amos.tool.PropertiesUtil;

public class MailBankMapUtils {
    public static Properties properties = PropertiesUtil.getPropsByEmail("mail_bank_map");

    public static String getBankNameByEmailAddress(String email) {
        return properties.getProperty(email);
    }
}
