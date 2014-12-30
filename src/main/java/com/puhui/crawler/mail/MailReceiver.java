package com.puhui.crawler.mail;

import java.io.File;
import java.io.IOException;
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
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.amos.tool.PropertiesUtil;
import com.puhui.crawler.Response;
import com.puhui.crawler.util.DateUtils;
import com.puhui.crawler.util.HttpUtils;

public class MailReceiver {
    private Logger logger = Logger.getLogger(MailReceiver.class);
    public final String POP3 = "pop3";
    public final String IMAP = "imap";
    public final String INBOX = "INBOX";
    private final String CONTENT_TYPE_TEXT_HTML = "text/html";
    private final String MAIL_FORM_REGEX = PropertiesUtil.getProps("mail.from");
    private final String MAIL_SUBJECT_REGEX = PropertiesUtil.getProps("mail.subject");
    private final Pattern MAIL_FORM_PATTER = Pattern.compile(MAIL_FORM_REGEX);
    private final Pattern MAIL_SUBJECT_PATTER = Pattern.compile(MAIL_SUBJECT_REGEX);
    private final String BANK_BILLS_DIR = PropertiesUtil.getProps("bank.bills.dir");
    private static final boolean RECEIVE_ALL = Boolean.valueOf(PropertiesUtil.getProps("bank.bills.receive.all"));
    private String username;
    private String password;
    private static final int MONTH_AGO = NumberUtils.toInt(PropertiesUtil.getProps("mail.n.month.ago"), 3);
    private int monthAgo;
    private String sendDateString;
    private int mailConut = 0;

    public MailReceiver(String username, String password) {
        this(username, password, MONTH_AGO);
    }

    public MailReceiver(String username, String password, int monthAgo) {
        this.username = username;
        this.password = password;
        this.monthAgo = monthAgo;
    }

    /**
     * 获取最近几个月的符合条件的邮件
     * 
     * @author zhuyuhang
     * @throws Exception
     */
    public Response receiveMail() {
        logger.info("account info:[" + username + "," + password + "]");
        Properties props = PropertiesUtil.getPropsByEmail(username);
        String protocol = props.getProperty("mail.protocol");
        Session session = null;
        Store store = null;
        session = createSession(props, username, password);
        try {
            store = session.getStore(protocol == null ? POP3 : protocol);
            store.connect();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new Response(false, "登录失败");
        }

        Folder folder = null;
        try {
            folder = store.getFolder(INBOX);
            folder.open(Folder.READ_ONLY);
        } catch (MessagingException e) {
            logger.error(e.getMessage(), e);
            return new Response(false, "打开收件箱错误");
        }
        Date date = DateUtils.someMonthAgo(monthAgo);
        int length = 0;
        try {
            length = folder.getMessageCount();
        } catch (MessagingException e1) {
            logger.error(e1.getMessage(), e1);
            return new Response(false, "获取收件箱邮件数量错误");
        }
        logger.debug("MessageCount:" + length);
        for (int i = length; i > 0; i--) {// 按倒序来取
            try {
                Message msg = folder.getMessage(i);
                Date sendDate = msg.getSentDate();
                sendDateString = DateUtils.formatDate(sendDate, "yyyy-MM-dd HH:mm:ss");
                logger.debug(i + "\t" + sendDateString);
                if (RECEIVE_ALL) {
                    processMsg(msg);
                } else {
                    if (sendDate.after(date)) {
                        processMsg(msg);
                    } else {
                        break;
                    }
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        try {
            folder.close(true);
            store.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new Response(true, "共[" + mailConut + "]封有效邮件");
    }

    /**
     * 这里只提取了邮件的html部分 其它部分都忽略了
     * 
     * @author zhuyuhang
     * @param msg
     * @throws Exception
     */
    private void processMsg(Message msg) {
        try {
            Address[] address = msg.getFrom();
            if (address == null || address.length == 0) {
                return;
            }
            String from = getMailFromAddress(address[0].toString());
            String subject = msg.getSubject();
            if (MAIL_FORM_PATTER.matcher(from).matches() && MAIL_SUBJECT_PATTER.matcher(subject).matches()) {
                File file = createFile(from);
                String tmp = subject + "|" + this.username + "|" + this.sendDateString + "|" + from + "\r\n";
                FileUtils.write(file, tmp, HttpUtils.UTF_8);
                FileUtils.write(file, dumpPart(msg), HttpUtils.UTF_8, true);
                ++mailConut;
            }
        } catch (Exception e) {
            logger.error("处理[" + this.username + "," + this.password + "]的邮件发生错误", e);
        }
    }

    private String getMailFromAddress(String from) {
        if (from.indexOf("<") < 0) {
            return from;
        }
        int begin = from.lastIndexOf("<");
        if (begin == from.length() - 1) {
            return from;
        }
        return from.substring(from.lastIndexOf("<") + 1, from.lastIndexOf(">"));
    }

    /**
     * 提取html
     * 
     * @param p
     * @return
     * @throws MessagingException
     * @throws IOException
     * @throws Exception
     */
    public String dumpPart(Part p) throws MessagingException, IOException {
        String contentType = p.getContentType().toLowerCase();
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
        }
        return null;
    }

    public Session createSession(Properties properties, String username, String password) {
        properties.put("rsetbeforequit", true);
        properties.put("mail.pop3.ssl.trust", "*");
        Session session = Session.getInstance(properties, new MailAuthenticator(username, password));
        return session;
    }

    private File createFile(String from) {
        File dir = new File(BANK_BILLS_DIR, DateUtils.formatDate(new Date()));
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }
        return new File(dir, "bank_" + MailBankMapUtils.getBankNameByEmailAddress(from) + "_"
                + DateUtils.formatDate(new Date(), "yyyyMMddHHmmssS") + ".html");
    }
}
