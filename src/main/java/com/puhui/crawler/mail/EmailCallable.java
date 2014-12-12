package com.puhui.crawler.mail;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.amos.tool.PropertiesUtil;
import com.puhui.crawler.util.DateUtils;
import com.puhui.crawler.util.HttpUtils;

@SuppressWarnings("hiding")
public class EmailCallable<Void> implements Callable<Void> {
    private static final Logger logger = Logger.getLogger(EmailCallable.class);
    private final String CONTENT_TYPE_TEXT_HTML = "text/html";
    private final String MAIL_FORM_REGEX = PropertiesUtil.getProps("mail.from");
    private final String MAIL_SUBJECT_REGEX = PropertiesUtil.getProps("mail.subject");
    private final Pattern MAIL_FORM_PATTER = Pattern.compile(MAIL_FORM_REGEX);
    private final Pattern MAIL_SUBJECT_PATTER = Pattern.compile(MAIL_SUBJECT_REGEX);
    private final String BANK_BILLS_DIR = PropertiesUtil.getProps("bank_bills_dir");
    private Folder folder;
    private Date sometimeAgo;
    private int index;
    private int length;

    public EmailCallable(Folder folder, Date sometimeAgo, int index, int length) {
        this.folder = folder;
        this.sometimeAgo = sometimeAgo;
        this.index = index;
        this.length = length;
    }

    @Override
    public Void call() {
        if (index > length) {
            int tmp = index;
            while (index > (tmp - length)) {
                try {
                    Message msg = folder.getMessage(index);
                    Date sendDate = msg.getSentDate();
                    logger.debug(Thread.currentThread().getName() + "\t" + index + "\t"
                            + DateUtils.formatDate(sendDate, "yyyy-MM-dd HH:mm:ss"));
                    if (sendDate.after(sometimeAgo)) {
                        processMsg(msg);
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                index = index - 1;
            }
        } else {
            while (index > 0) {
                try {
                    Message msg = folder.getMessage(index);
                    Date sendDate = msg.getSentDate();
                    logger.debug(index + "\t" + DateUtils.formatDate(sendDate, "yyyy-MM-dd HH:mm:ss"));
                    if (sendDate.after(sometimeAgo)) {
                        processMsg(msg);
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                index = index - 1;
            }
        }
        return null;
    }

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
        return new File(BANK_BILLS_DIR, "bank_" + MailBankMapUtils.getBankNameByEmailAddress(from) + "_"
                + DateUtils.formatDate(new Date(), "yyyyMMddHHmmssS") + ".html");
    }

}
