package com.puhui.crawler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.amos.tool.PropertiesUtil;
import com.puhui.crawler.mobile.CM_BJ_MobileFetcher;
import com.puhui.crawler.util.HttpUtils;
import com.puhui.crawler.util.SSLUtils;

/**
 * 征信报告　
 * 
 * @author zhuyuhang
 */
public class CreditReport {
    private static final Logger logger = Logger.getLogger(CM_BJ_MobileFetcher.class);
    private CloseableHttpClient client;
    private CookieStore cookieStore = new BasicCookieStore();
    private static SSLConnectionSocketFactory sslcsf = SSLUtils.createSSLConnectionSocketFactory(
            CM_BJ_MobileFetcher.class.getResourceAsStream("/certs/pbccrc.store"), "123456");
    private int retryTimes = 0;

    public CreditReport() {
        client = HttpUtils.getHttpClient(sslcsf, cookieStore);
    }

    public File loadCaptchaCode() {
        String url = "https://ipcrs.pbccrc.org.cn/imgrc.do?" + System.currentTimeMillis();
        return HttpUtils.getCaptchaCodeImage(client, url);
    }

    /**
     * @author zhuyuhang
     * @param loginname
     *            用户名
     * @param password
     *            密码
     * @param tradeCode
     *            查询码
     * @throws Exception
     */
    public void loadCreditReport(String loginname, String password, String tradeCode) throws Exception {
        loadCreditReport(loginname, password, tradeCode, null);
    }

    /**
     * @author zhuyuhang
     * @param username
     *            用户名
     * @param password
     *            密码
     * @param tradeCode
     *            查询码
     * @param captchaCode
     *            图片验证码
     * @throws Exception
     */
    public boolean loadCreditReport(String loginname, String password, String tradeCode, String captchaCode) {
        if (retryTimes > 5) {
            return false;
        } else {
            logger.error(loginname + "，第[" + retryTimes + "]次尝试获取");
        }
        retryTimes++;
        File desc = null;
        try {
            if (StringUtils.isBlank(captchaCode)) {
                logger.debug("自动识别验证码");
                String url = "https://ipcrs.pbccrc.org.cn/imgrc.do?" + Math.random();
                HttpGet get = HttpUtils.get(url);
                CloseableHttpResponse response = client.execute(get);
                File codeFile = new File(PropertiesUtil.getProps("mobile.captcha.dir"), System.currentTimeMillis()
                        + ".jpg");
                FileUtils.copyInputStreamToFile(response.getEntity().getContent(), codeFile);
                response.close();
                captchaCode = Ocr.getCodeFromImage(codeFile, "mycode", true);
            }

            logger.debug("登录开始");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("page", "1"));
            formparams.add(new BasicNameValuePair("method", "login"));
            formparams.add(new BasicNameValuePair("date", System.currentTimeMillis() + ""));
            formparams.add(new BasicNameValuePair("loginname", loginname));
            formparams.add(new BasicNameValuePair("password", password));
            formparams.add(new BasicNameValuePair("_@IMGRC@_", captchaCode));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Charset.forName("gbk"));
            HttpPost httppost = new HttpPost("https://ipcrs.pbccrc.org.cn/login.do");
            httppost.setEntity(entity);
            CloseableHttpResponse response = client.execute(httppost);
            response.close();
            logger.debug("登录结束");

            logger.debug("抓取token开始");
            HttpGet get = new HttpGet("https://ipcrs.pbccrc.org.cn/simpleReportAction.do?method=welcome");
            response = client.execute(get);
            String html = EntityUtils.toString(response.getEntity());
            response.close();
            Document doc = Jsoup.parse(html);
            Elements els = doc.select("input[name=org.apache.struts.taglib.html.TOKEN]");
            String token = null;
            if (!els.isEmpty()) {
                token = (els.get(0).attr("value"));
            }
            logger.debug("抓取token结束。值:" + token);
            logger.debug("抓取信用报告开始");
            get = new HttpGet("https://ipcrs.pbccrc.org.cn/simpleReportAction.do?method=view&tradeCode=" + tradeCode
                    + "&org.apache.struts.taglib.html.TOKEN=" + token);
            logger.debug(get.getURI().toString());
            response = client.execute(get);
            desc = new File(PropertiesUtil.getProps("credit.report.dir"), loginname + ".html");
            FileUtils.write(desc, EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8), HttpUtils.UTF_8);
            if (desc.length() >= 8 * 1024) {
                return true;
            } else {
                desc = null;
            }
            response.close();
            logger.debug("抓取信用报告结束");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (desc == null) {
            if (loadCreditReport(loginname, password, tradeCode, null)) {
                return true;
            }
        }
        return false;
    }

    public void close() {
        if (this.client != null) {
            try {
                this.client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String loginname = "";
        String password = "";
        String tradeCode = "";
        new CreditReport().loadCreditReport(loginname, password, tradeCode);
    }
}
