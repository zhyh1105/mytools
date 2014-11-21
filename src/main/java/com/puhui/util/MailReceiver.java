package com.puhui.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.puhui.util.mail.MailAuthenticator;

public class MailReceiver {
    private Logger logger = Logger.getLogger(MailReceiver.class);
    public final String PROTOCOL = "pop3";
    public final String INBOX = "INBOX";
    private final String CONTENT_TYPE_TEXT_HTML = "text/html";
    private final String MAIL_FORM_REGEX = PropertiesUtil.getProps("mail.from");
    private final String MAIL_SUBJECT_REGEX = PropertiesUtil.getProps("mail.subject");
    private final Pattern MAIL_FORM_PATTER = Pattern.compile(MAIL_FORM_REGEX);
    private final Pattern MAIL_SUBJECT_PATTER = Pattern.compile(MAIL_SUBJECT_REGEX);
    private final String BANK_BILLS_DIR = PropertiesUtil.getProps("bank_bills_dir");

    public MailReceiver(String username, String password) throws Exception {
        this.receiveMail(username, password);
    }

    /**
     * 获取最近两个月的符合条件的邮件
     * 
     * @author zhuyuhang
     * @param username
     *            邮箱账户
     * @param password
     *            邮箱密码
     * @throws Exception
     */
    public void receiveMail(String username, String password) throws Exception {
        receiveMail(username, password, 2);
    }

    /**
     * 获取最近几个月的符合条件的邮件
     * 
     * @author zhuyuhang
     * @param username
     *            邮箱账户
     * @param password
     *            邮箱密码
     * @param monthAgo
     *            前几个月
     * @throws Exception
     */
    public void receiveMail(String username, String password, int monthAgo) throws Exception {
        Session session = createSession(PropertiesUtil.getPropsByEmail(username), username, password);
        Store store = session.getStore(PROTOCOL);
        store.connect();
        Folder folder = store.getFolder(INBOX);
        folder.open(Folder.READ_ONLY);
        Message[] msgs = folder.getMessages();
        Date date = DateUtils.someMonthAgo(monthAgo);
        int length = folder.getMessageCount();
        for (int i = length - 1; i >= 0; i--) {// 按倒序来取
            try {
                Message msg = folder.getMessage(i);
                Date sendDate = msg.getSentDate();
                logger.debug(DateUtils.formatDate(sendDate, "yyyy-MM-dd HH:mm:ss"));
                if (sendDate.after(date)) {
                    processMsg(msgs[i]);
                } else {
                    break;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        folder.close(true);
        store.close();
    }

    /**
     * 这里只提取了邮件的html部分 其它部分都忽略了
     * 
     * @author zhuyuhang
     * @param msg
     * @throws Exception
     */
    private void processMsg(Message msg) throws Exception {
        Address[] address = msg.getFrom();
        if (address == null || address.length == 0) {
            return;
        }
        String from = address[0].toString();
        String subject = msg.getSubject();
        if (MAIL_FORM_PATTER.matcher(from).matches() && MAIL_SUBJECT_PATTER.matcher(subject).matches()) {
            String msgContent = dumpPart(msg);
            File file = new File(BANK_BILLS_DIR, System.currentTimeMillis() + ".html");
            FileUtils.write(file, msgContent, true);
        }
    }

    /**
     * 提取html
     * 
     * @param p
     * @return
     * @throws Exception
     */
    public String dumpPart(Part p) throws Exception {
        String contentType = p.getContentType();
        Object o = p.getContent();
        if (o instanceof String) {// 这是纯文本
            if (contentType.contains(CONTENT_TYPE_TEXT_HTML)) {
                return (String) o;
            }
        } else if (o instanceof Multipart) {// multipart
            Multipart mp = (Multipart) o;
            int count = mp.getCount();
            for (int i = 0; i < count; i++) {
                String result = dumpPart(mp.getBodyPart(i));
                if (result != null) {
                    return result;
                }
            }
        } else if (o instanceof InputStream) {// 那么这就是附件了
        }
        return null;
    }

    private String processTextHtml(Part p) throws MessagingException, IOException {
        // TODO 是否转码
        return (String) p.getContent();
    }

    public Session createSession(Properties properties, String username, String password) {
        properties.put("rsetbeforequit", true);
        properties.put("mail.pop3.ssl.trust", "*");
        Session session = Session.getDefaultInstance(properties, new MailAuthenticator(username, password));
        return session;
    }
}
