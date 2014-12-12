package com.puhui.crawler.mail;

import org.apache.log4j.Logger;

import com.puhui.crawler.mobile.MobileFetcher;

public class MailReceiverManager {
    private static Logger logger = Logger.getLogger(MailReceiverManager.class);

    public static void receiveEmails(final String username, final String password) {
        MobileFetcher.EXECUTOR_SERVICE.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    new MailReceiver(username, password);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }
}
