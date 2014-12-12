package com.puhui.crawler.mobile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.puhui.crawler.util.DateUtils;
import com.puhui.crawler.util.HttpUtils;
import com.puhui.crawler.util.SSLUtils;

/**
 * 湖北移动
 * 
 * @author zhuyuhang
 */
public class CM_HB_MobileFetcher extends MobileFetcher {
    private Logger logger = Logger.getLogger(CM_HB_MobileFetcher.class);
    private String ssoSessionID;
    private CloseableHttpClient client;
    private CloseableHttpClient clientWithSSL;
    private static String storePasswd = "123456";
    private static final String PATTERN_10086 = "yyyyMM";
    private CookieStore cookieStore = new BasicCookieStore();
    private static SSLConnectionSocketFactory sscsf = SSLUtils.createSSLConnectionSocketFactory(
            CM_HB_MobileFetcher.class.getResourceAsStream("/certs/hb.ac.10086.cn.keystore"), storePasswd);

    public CM_HB_MobileFetcher() {
        this.client = HttpUtils.getHttpClient(true, cookieStore);
        this.clientWithSSL = HttpUtils.getHttpClient(sscsf, cookieStore);
    }

    @Override
    public boolean login(String phone, String password, String rnum) {
        super.login(phone, password, rnum);
        return this.go();
    }

    @Override
    public boolean loadBills() {
        this.submitBillTasks();// 获取账单
        return true;
    }

    private boolean go() {
        try {
            return this.prepare() && this.login();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    private boolean prepare() throws ClientProtocolException, IOException {
        try {
            String url = "https://hb.ac.10086.cn/login";
            HttpGet get = HttpUtils.get(url);
            client.execute(get).close();
            logger.debug("预备");
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 登录
     * 
     * @author zhuyuhang
     * @throws IOException
     */
    public boolean login() {
        try {
            String url = "https://hb.ac.10086.cn/SSO/loginbox?!=1";
            Map<String, Object> params = new HashMap<>();
            params.put("accountType", 0);
            params.put("username", this.getPhone());
            params.put("passwordType", "1");
            params.put("password", this.getPassword());
            params.put("smsRandomCode", null);
            params.put("emailusername", "请输入登录帐号");
            params.put("emailpassword", null);
            params.put("validateCode", this.getCaptchaCode());
            params.put("action", "/SSO/loginbox");
            params.put("style", "mymobile");
            params.put("service", "my");
            params.put("continue", null);
            params.put("submitMode", "login");
            params.put("loginName", this.getPhone());
            params.put("password", this.getPassword());
            params.put("guestIP", "202.103.10.10");
            HttpPost post = HttpUtils.post(url, params);
            CloseableHttpResponse response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            logger.debug(responseString);
            response.close();

            post = HttpUtils.buildPostFromHtml(responseString, "#sso");
            client.execute(post).close();

            url = "http://www.hb.10086.cn/my/index.action";
            HttpUtils.executeGet(client, url);

            url = "http://www.hb.10086.cn/my";
            HttpUtils.executeGet(client, url);
            // 积分
            url = "http://www.hb.10086.cn/my/score/customerAjax.action?_=1418191107717";
            responseString = HttpUtils.executeGetWithResult(client, url);
            logger.debug(responseString);
            // 当前消费情况
            url = "http://www.hb.10086.cn/my/balance/queryBalance.action";
            responseString = HttpUtils.executeGetWithResult(client, url);
            logger.debug(responseString);

            logger.debug("hb10086自动登录确认");
            url = "http://www.hb.10086.cn/service/autoLogin.action?auto=true";
            responseString = HttpUtils.executeGetWithResult(client, url);
            logger.debug(responseString);
            post = HttpUtils.buildPostFromHtml(responseString, "#frmPost");
            responseString = HttpUtils.executePostWithResult(client, post);
            logger.debug(responseString);
            post = HttpUtils.buildPostFromHtml(responseString, "#sso");
            responseString = HttpUtils.executePostWithResult(client, post);
            logger.debug(responseString);

            String cmtokenid = HttpUtils.getFirstCookie(cookieStore, "cmtokenid");
            if (cmtokenid == null) {
                return false;
            }
            this.ssoSessionID = getSSOSessionID(cmtokenid);
            logger.debug("hb10086登录，ssoSessionID:" + ssoSessionID);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    protected void submitBillTasks() {
        try {
            // 自动确认登录
            this.loginSecondCheck();
            super.submitBillTasks();
            this.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * 二次登录确认
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    private void loginSecondCheck() throws ClientProtocolException, IOException {
        // TODO 再次短信验证
    }

    /**
     * 前六个月历史账单
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    protected void hisBill() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
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
    private void hisBill(Date month) {
        // 实时 get
        String ms = DateUtils.formatDate(month, PATTERN_10086);
        logger.debug("历史账单" + ms);
        Map<String, Object> params = new HashMap<String, Object>();
        String url = "http://www.hb.10086.cn/my/billdetails/showbillMixQuery.action?postion=outer";
        if (ms.equals(DateUtils.formatDate(new Date(), PATTERN_10086))) {// 实时
            params.put("qryMonthType", "current");
        } else {
            params.put("qryMonthType", "history");
        }
        params.put("theMonth", ms);
        params.put("menuid", "myBill");
        params.put("groupId", "tabs3");
        url += HttpUtils.buildParamString(params);
        HttpPost post = HttpUtils.post(url);
        try {
            CloseableHttpResponse response = client.execute(post);
            writeToFile(createTempFile(BILL_TYPE_HISBILL), response.getEntity());
            response.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 前三个月的通话详单
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    protected void gsm() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
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
    private void gsm(Date month) {
        Map<String, Object> params = new HashMap<>();
        String ms = DateUtils.formatDate(month, PATTERN_10086);
        logger.debug("通话详单" + ms);
        params.put("checkMonth", ms);
        params.put("detailType", "gsm");
        params.put("ssoSessionID", ssoSessionID);

        String url = "https://cmodsvr1.hb.chinamobile.com/PortalCMOD/detail/detail.do?1=1";
        url += HttpUtils.buildParamString(params);
        try {
            HttpGet get = HttpUtils.get(url);
            CloseableHttpResponse response = clientWithSSL.execute(get);
            writeToFile(createTempFile(BILL_TYPE_GSM), response.getEntity());
            response.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 短信详单
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    protected void sms() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
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
    private void sms(Date month) {
        Map<String, Object> params = new HashMap<>();
        String ms = DateUtils.formatDate(month, PATTERN_10086);
        logger.debug("短信详单" + ms);
        params.put("checkMonth", ms);
        params.put("detailType", "sms");
        params.put("ssoSessionID", ssoSessionID);

        String url = "https://cmodsvr1.hb.chinamobile.com/PortalCMOD/detail/detail.do?1=1";
        url += HttpUtils.buildParamString(params);
        try {
            HttpGet get = HttpUtils.get(url);
            CloseableHttpResponse response = clientWithSSL.execute(get);
            writeToFile(createTempFile(BILL_TYPE_SMS), response.getEntity());
            response.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void logout() {
        try {
            logger.debug("退出登录");
            HttpUtils.executeGet(client, "https://hb.ac.10086.cn/logout");
        } catch (Exception e) {
            logger.error("退出登录", e);
        }
    }

    /**
     * 释放资源
     * 
     * @author zhuyuhang
     */
    @Override
    public void close() {
        super.close();
        try {
            this.logout();
            if (this.client != null) {
                this.client.close();
            }
            if (this.clientWithSSL != null) {
                this.client.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getSSOSessionID(String cmtokenid) {
        return cmtokenid.split("@")[0];
    }

    @Override
    public boolean hasCaptcha() {
        return true;
    }

    @Override
    public File loadCaptchaCode() {
        String url = "https://hb.ac.10086.cn/SSO/img?codeType=0&rand=" + System.currentTimeMillis();
        return getCaptchaCodeImage(client, url);
    }

    @Override
    public boolean checkCaptchaCode() {
        return true;
    }

    @Override
    public boolean hasRandomcode() {
        return false;
    }

    @Override
    public boolean sendRandombySms() {
        return false;
    }

    @Override
    public boolean validateRandomcode(String randomCode) {
        return false;
    }

    @Override
    public String getIspSimpleName() {
        return ISP_CM;
    }

    @Override
    public String getAreaSimpleName() {
        return "hb";
    }

    @Override
    protected void personalInfo() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void mzlog() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void addvalue() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void rc() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void gprs() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void mon() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void currFee() {
        // TODO Auto-generated method stub

    }
}
