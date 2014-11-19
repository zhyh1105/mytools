package com.puhui.util;

import java.io.IOException;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import org.apache.commons.lang3.StringUtils;

import com.amos.tool.PropertiesUtil;
import com.puhui.util.mail.MailAuthenticator;

public class MailReceiver {
    private static final String PROTOCOL = "pop3";
    private static final String INBOX = "INBOX";
    private static final String CONTENT_TYPE_MULTIPART_MIXED = "multipart/mixed";
    private static final String CONTENT_TYPE_MULTIPART_MULTIPART_RELATED = "multipart/related";
    private static final String CONTENT_TYPE_MULTIPART_MULTIPART_ALTERNATIVE = "multipart/alternative";
    private static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    private static final String CONTENT_TYPE_TEXT_HTML = "text/html";

    public static void main(String args[]) throws Exception {
        String username = "584440082@qq.com";
        String password = "puhuijinrong17";
        Session session = createSession(PropertiesUtil.getPropsByEmail(username), username, password);
        Store store = session.getStore(PROTOCOL);
        store.connect();
        Folder folder = store.getFolder(INBOX);
        folder.open(Folder.READ_ONLY);
        // SearchTerm searchTerm = new AndTerm(new FromStringTerm("@amazon.cn"),
        // new SubjectTerm("已经发货"));
        SearchTerm searchTerm = new SubjectTerm("银行");
        Message[] msgs = folder.search(searchTerm);
        int i = 0;
        for (Message msg : msgs) {
            System.out.println(msg.getContentType());
            String subject = msg.getSubject();
            String from = msg.getFrom()[0].toString();
            // String msgContent = processMsg(msg);
            System.out.printf("%d\n\t%s\n\t\t%s\n", i, from, subject);
            // File file = new File("D:\\tmp\\mail", i + ".html");
            // FileUtils.write(file, from);
            // FileUtils.write(file, "\n\r", true);
            // FileUtils.write(file, subject, true);
            // FileUtils.write(file, "\n\r", true);
            // FileUtils.write(file, msgContent, true);
            ++i;
        }
        folder.close(true);
        store.close();
    }

    public static String processMsg(Message msg) throws MessagingException, IOException {
        String contentType = msg.getContentType();
        if (contentType != null) {
            contentType = contentType.toLowerCase();
            if (contentType.contains(CONTENT_TYPE_TEXT_HTML) || contentType.contains(CONTENT_TYPE_TEXT_PLAIN)) {
                return msg.getContent().toString();
            } else if (contentType.contains(CONTENT_TYPE_MULTIPART_MIXED)) {
                return processMultipartMixed((Multipart) msg.getContent());
            } else if (contentType.contains(CONTENT_TYPE_MULTIPART_MULTIPART_RELATED)) {
                return processMultipartRelated((Multipart) msg.getContent());
            } else if (contentType.contains(CONTENT_TYPE_MULTIPART_MULTIPART_ALTERNATIVE)) {
                return processMultipartAlternative((Multipart) msg.getContent());
            }
        }
        return msg.getContent().toString();
    }

    /**
     * 处理multipart/mixed
     * 
     * @author zhuyuhang
     * @param part
     * @throws IOException
     * @throws MessagingException
     */
    public static String processMultipartMixed(Multipart part) throws MessagingException, IOException {
        for (int i = 0; i < part.getCount(); i++) {
            BodyPart bp = part.getBodyPart(i);
            String contentType = bp.getContentType();
            if (StringUtils.isNotBlank(contentType)) {
                if (contentType.contains(CONTENT_TYPE_MULTIPART_MULTIPART_RELATED)) {
                    return processMultipartRelated((Multipart) bp.getContent());
                } else if (contentType.contains(CONTENT_TYPE_MULTIPART_MULTIPART_ALTERNATIVE)) {
                    return processMultipartAlternative((Multipart) bp.getContent());
                } else if (contentType.contains(CONTENT_TYPE_TEXT_HTML)) {
                    return bp.getContent().toString();
                } else if (contentType.contains(CONTENT_TYPE_TEXT_PLAIN)) {// 纯文本的内容我们不需要　
                }
            }
        }
        return null;
    }

    /**
     * 处理multipart/related
     * 
     * @author zhuyuhang
     * @param part
     * @throws IOException
     * @throws MessagingException
     */
    public static String processMultipartRelated(Multipart part) throws MessagingException, IOException {
        for (int i = 0; i < part.getCount(); i++) {
            BodyPart bp = part.getBodyPart(i);
            String contentType = bp.getContentType();
            if (StringUtils.isNotBlank(contentType)) {
                if (contentType.contains(CONTENT_TYPE_MULTIPART_MULTIPART_ALTERNATIVE)) {// 这里我们只处理html
                    return processMultipartAlternative((Multipart) bp.getContent());
                } else if (contentType.contains(CONTENT_TYPE_TEXT_HTML)) {
                    return bp.getContent().toString();
                } else if (contentType.contains(CONTENT_TYPE_TEXT_PLAIN)) {// 纯文本的内容我们不需要　
                }
            }
        }
        return null;
    }

    /**
     * 处理multipart/alternative
     * 
     * @author zhuyuhang
     * @param part
     * @throws MessagingException
     * @throws IOException
     */
    public static String processMultipartAlternative(Multipart part) throws MessagingException, IOException {
        for (int i = 0; i < part.getCount(); i++) {
            BodyPart bp = part.getBodyPart(i);
            String contentType = bp.getContentType();
            if (StringUtils.isNotBlank(contentType)) {
                if (contentType.contains(CONTENT_TYPE_TEXT_HTML)) {// 这里我们只处理html
                    return bp.getContent().toString();
                } else if (contentType.contains(CONTENT_TYPE_TEXT_PLAIN)) {// 纯文本的内容我们不需要　
                }
            }
        }
        return null;
    }

    public static String processTextHtml(BodyPart part) throws MessagingException, IOException {
        return part.getContent().toString();
    }

    public static Session createSession(Properties properties, String username, String password) {
        properties.put("rsetbeforequit", true);
        properties.put("mail.pop3.ssl.trust", "*");
        Session session = Session.getDefaultInstance(properties, new MailAuthenticator(username, password));
        return session;
    }
}
