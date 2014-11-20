package com.puhui.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import javax.mail.search.SubjectTerm;

import org.apache.commons.io.FileUtils;

import com.puhui.util.mail.MailAuthenticator;

public class MailReceiver {
    private static final String PROTOCOL = "pop3";
    private static final String INBOX = "INBOX";
    private static final String CONTENT_TYPE_TEXT_HTML = "text/html";

    public static void main(String args[]) throws Exception {
        String username = "5844400892@qq.com";
        String password = "abcdefg";
        receiveMail(username, password);
    }

    public static void receiveMail(String username, String password) throws Exception {
        Session session = createSession(PropertiesUtil.getPropsByEmail(username), username, password);
        Store store = session.getStore(PROTOCOL);
        store.connect();
        Folder folder = store.getFolder(INBOX);
        folder.open(Folder.READ_ONLY);
        Message[] msgs = folder.search(makeSearchTerm());
        // Message[] msgs = folder.getMessages();
        for (Message msg : msgs) {
            String msgContent = dumpPart(msg);
            File file = new File("D:\\tmp\\hotmail", System.currentTimeMillis() + ".html");
            FileUtils.write(file, msgContent, true);
        }
        folder.close(true);
        store.close();
    }

    private static SearchTerm makeSearchTerm() throws AddressException {
        FromTerm fromTerm = new FromTerm(new InternetAddress("ccsvc@message.cmbchina.com"));
        SubjectTerm subjectTerm = new SubjectTerm("招商银行信用卡电子账单");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 2);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 2);
        SearchTerm fromAndSubject = new AndTerm(fromTerm, subjectTerm);
        SentDateTerm sentDateTerm = new SentDateTerm(ComparisonTerm.GE, calendar.getTime());
        SearchTerm searchTerm = new AndTerm(sentDateTerm, fromAndSubject);
        // SearchTerm searchTerm = new SubjectTerm("银行");
        return searchTerm;
    }

    /**
     * 提取html
     * 
     * @param p
     * @return
     * @throws Exception
     */
    public static String dumpPart(Part p) throws Exception {
        String contentType = p.getContentType();
        Object o = p.getContent();
        if (o instanceof String) {// 这是纯文本
            if (contentType.contains(CONTENT_TYPE_TEXT_HTML)) {
                return (String) o;
            }
        } else if (o instanceof Multipart) {// 这是混合部分
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

    private static String processTextHtml(Part p) throws MessagingException, IOException {
        // TODO 是否转码
        return (String) p.getContent();
    }

    public static Session createSession(Properties properties, String username, String password) {
        properties.put("rsetbeforequit", true);
        properties.put("mail.pop3.ssl.trust", "*");
        Session session = Session.getDefaultInstance(properties, new MailAuthenticator(username, password));
        return session;
    }
}
