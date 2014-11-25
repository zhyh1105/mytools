package com.puhui.crawler;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.puhui.util.DateUtils;
import com.puhui.util.HttpUtils;
import com.puhui.util.ProcessUtils;
import com.puhui.util.PropertiesUtil;
import com.puhui.util.SSLUtils;

public class CM_10086 {
    private Logger logger = Logger.getLogger(CM_10086.class);
    private String phone;
    private String password;
    private String rnum;
    private String ssoSessionID;
    private CloseableHttpClient client;
    private CloseableHttpClient client2;
    private static String storePasswd = "123456";
    private static final String PATTERN_10086 = "yyyy.MM";
    private CookieStore cookieStore = new BasicCookieStore();
    private static SSLConnectionSocketFactory sscsf = SSLUtils.createSSLConnectionSocketFactory(
            CM_10086.class.getResourceAsStream("/certs/cmodsvr1.bj.chinamobile.com.keystore"), storePasswd);

    public CM_10086(String phone, String password) throws Exception {
        this.phone = phone;
        this.password = password;
        this.client = HttpUtils.getHttpClient(true, cookieStore);
        this.client2 = HttpUtils.getHttpClient(sscsf, cookieStore);

        this.prepare();// 预备
        this.isShowValidateRnum();// 是否显示验证码
        this.bmccMobile();// 验证手机号
        this.validateIp();// 验证ip
        this.rnumCheck();// 验证验证码
        this.login();// 登录
        this.getBills();// 获取账单
        this.close();
    }

    public void prepare() throws ClientProtocolException, IOException {
        String url = "https://bj.ac.10086.cn/login";
        HttpGet get = HttpUtils.get(url);
        client.execute(get).close();

        url = "https://bj.ac.10086.cn/ac/cmsso/iloginnew.jsp";
        get = HttpUtils.get(url);
        client.execute(get).close();

        cookieStore.addCookie(new BasicClientCookie("c_mobile", phone));
        cookieStore.addCookie(new BasicClientCookie("login_mobile", phone));
    }

    /**
     * 是否显示验证码
     * 
     * @author zhuyuhang
     * @throws IOException
     * @throws ClientProtocolException
     */
    public void isShowValidateRnum() throws ClientProtocolException, IOException {
        String url = "https://bj.ac.10086.cn/ac/IsShowValidateRnum";
        Map<String, Object> params = new HashMap<>();
        params.put("phone", phone);
        client.execute(HttpUtils.post(url, params)).close();
    }

    /**
     * 验证BmccMobile
     * 
     * @author zhuyuhang
     * @throws Exception
     */
    public boolean bmccMobile() {
        try {
            String url = "https://bj.ac.10086.cn/ac/BmccMobile";
            Map<String, Object> params = new HashMap<>();
            params.put("mobile", phone);
            HttpPost post = HttpUtils.post(url, params);
            CloseableHttpResponse response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity()).trim();
            response.close();

            JSONObject json = JSON.parseObject(responseString);
            if (json.containsKey("BMCC_MOB") && "OK".equalsIgnoreCase(json.getString("BMCC_MOB"))) {
                return true;
            } else {
                logger.error(responseString);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 验证IP
     * 
     * @author zhuyuhang
     * @throws IOException
     * @throws Exception
     */
    public boolean validateIp() throws IOException {
        try {
            String url = "https://bj.ac.10086.cn/ac/ValidateIp";
            CloseableHttpResponse response = client.execute(HttpUtils.post(url));
            String responseString = EntityUtils.toString(response.getEntity());
            response.close();
            JSONObject json = JSON.parseObject(responseString);
            if (json.containsKey("val-ip") && "ok".equalsIgnoreCase(json.getString("val-ip"))) {
                return true;
            } else {
                logger.error(responseString);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * @author zhuyuhang
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public boolean rnumCheck() throws ClientProtocolException, IOException {
        try {
            // 获取验证码
            String url = "https://bj.ac.10086.cn/ac/ValidateNum?smartID=" + System.currentTimeMillis();
            HttpGet get = HttpUtils.get(url);
            CloseableHttpResponse response = client.execute(get);
            File codeFile = new File("D:/tmp/10086/num/code.jpg");
            FileUtils.copyInputStreamToFile(response.getEntity().getContent(), codeFile);
            response.close();

            Desktop.getDesktop().open(codeFile);
            this.rnum = JOptionPane.showInputDialog("输入验证码");
            ProcessUtils.killDllhost();// 关闭图片查看器 妹的
            // 验证验证码
            url = "https://bj.ac.10086.cn/ac/ValidateRnum";
            Map<String, Object> params = new HashMap<>();
            params.put("user", phone);
            params.put("phone", phone);
            params.put("rnum", this.rnum);
            params.put("service", "www.bj.10086.cn");
            params.put("ssoLogin", "yes");
            params.put("loginMode", "1");
            params.put("loginMethod", "1");

            HttpPost post = HttpUtils.post(url, params);
            response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            response.close();
            JSONObject json = JSON.parseObject(responseString);
            if (json.containsKey("rnum-check") && "ok".equalsIgnoreCase(json.getString("rnum-check"))) {
                return true;
            } else {
                logger.error(responseString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 登录
     * 
     * @author zhuyuhang
     * @throws IOException
     */
    public boolean login() throws IOException {
        try {
            String url = "https://bj.ac.10086.cn/ac/CmSsoLogin?1=1";
            Map<String, Object> params = new HashMap<>();
            params.put("user", this.phone);
            params.put("phone", this.phone);
            params.put("backurl", "http://www.bj.10086.cn/my");
            params.put("continue", "http://www.bj.10086.cn/my");
            params.put("style", "BIZ_LOGINBOX");
            params.put("service", "www.bj.10086.cn");
            params.put("box", null);
            params.put("target", "_parent");
            params.put("ssoLogin", "yes");
            params.put("loginMode", "3");
            params.put("loginMethod", "1");
            params.put("loginName", this.phone);
            params.put("password", this.password);
            params.put("smsNum", "随机码");
            params.put("rnum", this.rnum);
            params.put("ckCookie", "on");
            HttpPost post = HttpUtils.post(url, params);
            CloseableHttpResponse response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            response.close();

            post = HttpUtils.buildPostFromHtml(responseString, "#loginAgain div form");
            client.execute(post).close();

            url = "https://bj.ac.10086.cn/ac/cmsso/redirect.jsp";
            HttpUtils.executeGet(client, url);

            url = "http://www.bj.10086.cn/my";
            HttpUtils.executeGet(client, url);

            url = "http://www.bj.10086.cn/service/fee/zdcx/";
            HttpUtils.executeGet(client, url);

            String cmtokenid = HttpUtils.getFirstCookie(cookieStore, "cmtokenid");
            if (cmtokenid == null) {
                return false;
            }
            this.ssoSessionID = getSSOSessionID(cmtokenid);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public void getBills() throws ClientProtocolException, IOException {
        String url = "https://cmodsvr1.bj.chinamobile.com/PortalCMOD/InnerInterFaceCiisHisBill";
        HttpGet get = HttpUtils.get(url);
        CloseableHttpResponse response = client2.execute(get);
        String responseString = EntityUtils.toString(response.getEntity());
        response.close();

        HttpPost post = HttpUtils.buildPostFromHtml(responseString);// https://bj.ac.10086.cn/ac/SamlCmAuthnResponse
        if (post != null) {
            response = client.execute(post);
            responseString = EntityUtils.toString(response.getEntity());
            response.close();
            post = HttpUtils.buildPostFromHtml(responseString);// https://cmodsvr1.bj.chinamobile.com/PortalCMOD/Login_Success.jsp?timemilllis=1416881038191
            if (post != null) {
                client2.execute(post).close();
                // 详单需要二次登录确认
                this.loginSecondCheck();
                // 历史账单
                this.hisBill();
                // 通话详单
                this.gsm();
                // 短信详单
                this.sms();
            }
        }
    }

    /**
     * 二次登录确认
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void loginSecondCheck() throws ClientProtocolException, IOException {
        Date date = new Date();
        for (int i = 0; i < 1; i++) {
            date = DateUtils.addMonths(date, -1);
            loginSecondCheck(date);
        }
    }

    /**
     * 二次登录确认
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void loginSecondCheck(Date month) throws ClientProtocolException, IOException {
        String url = "https://cmodsvr1.bj.chinamobile.com/PortalCMOD/LoginSecondCheck?1=1";
        Map<String, Object> params = new HashMap<>();
        params.put("searchType", "HisDetail");
        params.put("checkMonth", DateUtils.formatDate(month, PATTERN_10086));
        params.put("detailType", "rc");
        params.put("password", this.password);
        params.put("ssoSessionID", ssoSessionID);
        HttpUtils.executePost(client2, url, params);
    }

    /**
     * 前三个月历史账单
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void hisBill() throws ClientProtocolException, IOException {
        Date date = new Date();
        for (int i = 0; i < 3; i++) {
            hisBill(date);
            date = DateUtils.addMonths(date, -1);
        }
    }

    /**
     * 历史账单
     * 
     * @author zhuyuhang
     * @param month
     *            哪个月
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void hisBill(Date month) throws ClientProtocolException, IOException {
        // 实时 get
        String ms = DateUtils.formatDate(month, PATTERN_10086);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ssoSessionID", ssoSessionID);
        String url = "https://cmodsvr1.bj.chinamobile.com/PortalCMOD/bill/userbilland.do?1=1";
        if (ms.equals(DateUtils.formatDate(new Date(), PATTERN_10086))) {// 实时
            url = "https://cmodsvr1.bj.chinamobile.com/PortalCMOD/bill/userreal.do?1=1";
        } else {
            params.put("timer", 61);
            params.put("Month", DateUtils.formatDate(month, PATTERN_10086));
            params.put("livel", null);
        }
        url += HttpUtils.buildParamString(params);
        HttpGet get = HttpUtils.get(url);
        CloseableHttpResponse response = client2.execute(get);
        writeToFile(createTempFile("hisbill"), response.getEntity());
        response.close();
    }

    /**
     * 前三个月的通话详单
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void gsm() throws ClientProtocolException, IOException {
        Date date = new Date();
        for (int i = 0; i < 3; i++) {
            gsm(date);
            date = DateUtils.addMonths(date, -1);
        }
    }

    /**
     * 通话详单
     * 
     * @author zhuyuhang
     * @param month
     *            月份
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void gsm(Date month) throws ClientProtocolException, IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("checkMonth", DateUtils.formatDate(month, PATTERN_10086));
        params.put("detailType", "gsm");
        params.put("ssoSessionID", ssoSessionID);

        String url = "https://cmodsvr1.bj.chinamobile.com/PortalCMOD/detail/detail.do?1=1";
        url += HttpUtils.buildParamString(params);
        HttpGet get = HttpUtils.get(url);
        CloseableHttpResponse response = client2.execute(get);
        writeToFile(createTempFile("gsm"), response.getEntity());
        response.close();
    }

    /**
     * 短信详单
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void sms() throws ClientProtocolException, IOException {
        Date date = new Date();
        for (int i = 0; i < 3; i++) {
            sms(date);
            date = DateUtils.addMonths(date, -1);
        }
    }

    /**
     * 短信详单
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void sms(Date month) throws ClientProtocolException, IOException {
        // https://cmodsvr1.bj.chinamobile.com/PortalCMOD/detail/detail.do?checkMonth=2014.10&detailType=sms&ssoSessionID=2c9d82fa477d6ea30149e59938ae0b0e
        Map<String, Object> params = new HashMap<>();
        params.put("checkMonth", DateUtils.formatDate(month, PATTERN_10086));
        params.put("detailType", "sms");
        params.put("ssoSessionID", ssoSessionID);

        String url = "https://cmodsvr1.bj.chinamobile.com/PortalCMOD/detail/detail.do?1=1";
        url += HttpUtils.buildParamString(params);
        HttpGet get = HttpUtils.get(url);
        CloseableHttpResponse response = client2.execute(get);
        writeToFile(createTempFile("sms"), response.getEntity());
        response.close();
    }

    /**
     * 释放资源
     * 
     * @author zhuyuhang
     */
    private void close() {
        try {
            if (this.client != null) {
                this.client.close();
            }
            if (this.client2 != null) {
                this.client.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @author zhuyuhang
     * @param file
     * @param entity
     * @throws UnsupportedEncodingException
     * @throws ParseException
     * @throws IOException
     */
    private static void writeToFile(File file, HttpEntity entity) throws UnsupportedEncodingException, ParseException,
            IOException {
        String encoding = HttpUtils.GBK;
        Header contentType = entity.getContentType();
        if (contentType != null) {
            String tmp = HttpUtils.getCharsetFromContentType(contentType.getValue());
            if (tmp != null) {
                encoding = tmp;
            }
        }
        String content = EntityUtils.toString(entity, encoding);
        FileUtils.write(file, content, encoding);
    }

    /**
     * @author zhuyuhang
     * @param path
     * @return $10086.dir/path/System.currentTimeMillis().html
     */
    private File createTempFile(String path) {
        File file = new File(PropertiesUtil.getProps("10086.dir"), this.phone);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdir();
        }
        if (StringUtils.isNotBlank(path)) {
            file = new File(file, path);
            if (!file.exists() || !file.isDirectory()) {
                file.mkdir();
            }
        }
        file = new File(file, +System.currentTimeMillis() + ".html");
        return file;
    }

    private String getSSOSessionID(String cmtokenid) {
        return cmtokenid.split("@")[0];
    }
}
