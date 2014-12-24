package com.puhui.crawler.mail;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.amos.tool.PropertiesUtil;
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
        receiveMail(username, password, NumberUtils.toInt(PropertiesUtil.getProps("mail.n.month.ago"), 3));
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
        logger.debug("[" + username + "," + password + "]");
        Properties props = PropertiesUtil.getPropsByEmail(username);
        Session session = createSession(props, username, password);
        String protocol = props.getProperty("mail.protocol");
        Store store = session.getStore(protocol == null ? POP3 : protocol);
        store.connect();
        Folder folder = store.getFolder(INBOX);
        folder.open(Folder.READ_ONLY);
        Date date = DateUtils.someMonthAgo(monthAgo);
        int length = folder.getMessageCount();
        logger.debug("MessageCount:" + length);
        for (int i = length; i > 0; i--) {// 按倒序来取
            try {
                Message msg = folder.getMessage(i);
                Date sendDate = msg.getSentDate();
                logger.debug(i + "\t" + DateUtils.formatDate(sendDate, "yyyy-MM-dd HH:mm:ss"));
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
        String from = getMailFromAddress(address[0].toString());
        String subject = msg.getSubject();
        if (MAIL_FORM_PATTER.matcher(from).matches() && MAIL_SUBJECT_PATTER.matcher(subject).matches()) {
            FileUtils.write(createFile(from), dumpPart(msg), HttpUtils.UTF_8);
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
     * @throws Exception
     */
    public String dumpPart(Part p) throws Exception {
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

    private File createFile(String from) {
        File dir = new File(BANK_BILLS_DIR, DateUtils.formatDate(new Date()));
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }
        return new File(dir, "bank_" + MailBankMapUtils.getBankNameByEmailAddress(from) + "_"
                + DateUtils.formatDate(new Date(), "yyyyMMddHHmmssS") + ".html");
    }

    public Session createSession(Properties properties, String username, String password) {
        properties.put("rsetbeforequit", true);
        properties.put("mail.pop3.ssl.trust", "*");
        Session session = Session.getInstance(properties, new MailAuthenticator(username, password));
        return session;
    }
}
