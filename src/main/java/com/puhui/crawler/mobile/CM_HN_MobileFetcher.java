package com.puhui.crawler.mobile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.puhui.crawler.util.DateUtils;
import com.puhui.crawler.util.HttpUtils;

/**
 * 湖南移动
 * 
 * @author zhuyuhang
 */
public class CM_HN_MobileFetcher extends MobileFetcher {
    private Logger logger = Logger.getLogger(CM_HN_MobileFetcher.class);
    private CloseableHttpClient client;
    // private CloseableHttpClient clientWithSSL;
    // private static String storePasswd = "123456";
    private static final String PATTERN_10086 = "yyyyMM";
    private CookieStore cookieStore = new BasicCookieStore();
    private Map<String, Object> loginFormParams;
    private Map<String, Object> queryFormParams;

    // private static SSLConnectionSocketFactory sscsf =
    // SSLUtils.createSSLConnectionSocketFactory(
    // CM_HB_MobileFetcher.class.getResourceAsStream("/certs/hb.ac.10086.cn.keystore"),
    // storePasswd);

    public CM_HN_MobileFetcher() {
        this.client = HttpUtils.getHttpClient(true, cookieStore);
        // this.clientWithSSL = HttpUtils.getHttpClient(sscsf, cookieStore);
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
            String url = "https://www.hn.10086.cn/login/setGreyUserNo.jsp?mobileNum=" + getPhone();
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
            String url = "https://www.hn.10086.cn/login/dologin.jsp";
            loginFormParams.put("mobileNum", getPhone());
            loginFormParams.put("servicePWD", getPassword());
            loginFormParams.put("attachCode", getCaptchaCode());
            loginFormParams.put("randomNum", "");

            HttpPost post = HttpUtils.post(url, loginFormParams);
            post.addHeader("Origin", "https://www.hn.10086.cn");
            post.addHeader("Referer", "https://www.hn.10086.cn/login.jsp");
            CloseableHttpResponse response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            logger.debug(responseString);
            response.close();
            if (responseString.indexOf("top.location=\"/my/account/index.jsp") > -1) {
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    protected void submitBillTasks() {
        this.personalInfo();
        this.hisBill();
        this.gsm();
        this.sms();
        this.gprs();
        this.addvalue();
        this.mon();
        this.rc();
        this.mzlog();
        this.close();
    }

    /**
     * 历史账单
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
        try {
            String url = "https://www.hn.10086.cn/service/fee/monthBill.jsp";
            String responseString = HttpUtils.executeGetWithResult(client, url);
            Map<String, Object> params = HttpUtils.buildParamsFromHtml(responseString, "#serviceForm");
            params.put("startDate", ms);
            params.put("r", new Date());
            url = "https://www.hn.10086.cn/ajax/billservice/monthBillResult.jsp?1=1";
            url += HttpUtils.buildParamString(params);
            HttpPost post = HttpUtils.post(url);
            CloseableHttpResponse response = client.execute(post);
            writeToFile(createTempFile(BILL_TYPE_HISBILL), response.getEntity());
            response.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 通话详单
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    protected void gsm() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            commonFee(date, "1", BILL_TYPE_GSM, "通话详单");
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
    protected void sms() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            commonFee(date, "2", BILL_TYPE_SMS, "短信详单");
            date = DateUtils.addMonths(date, -1);
        }
    }

    @Override
    protected void personalInfo() {
        String url = "https://www.hn.10086.cn/my/account/index.jsp?recommendGiftPop=true";
        try {
            HttpGet get = HttpUtils.get(url);
            CloseableHttpResponse response = client.execute(get);
            writeToFile(createTempFile(BILL_TYPE_PERSONALINFO), response.getEntity());
            response.close();
        } catch (Exception e) {
            logger.error("获取个人信息失败", e);
        }
    }

    @Override
    protected void mzlog() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void addvalue() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            commonFee(date, "4", BILL_TYPE_ADDVALUE, "增值业务扣费记录");
            date = DateUtils.addMonths(date, -1);
        }
    }

    @Override
    protected void rc() {
    }

    @Override
    protected void gprs() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            commonFee(date, "3", BILL_TYPE_GPRS, "上网详单");
            date = DateUtils.addMonths(date, -1);
        }
    }

    @Override
    protected void mon() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            commonFee(date, "5", BILL_TYPE_MON, "代收业务扣费记录");
            date = DateUtils.addMonths(date, -1);
        }
    }

    @Override
    protected void currFee() {

    }

    /**
     * 通用获取账单方法 只有type在变
     * 
     * @author zhuyuhang
     * @param month
     *            月份
     * @param type
     *            账单类型
     * @param typeInFileName
     *            文件名称 gsm,sms,等
     * @param desc
     *            描述
     */
    private void commonFee(Date month, String type, String typeInFileName, String desc) {
        String ms = DateUtils.formatDate(month, PATTERN_10086);
        logger.debug(desc + ms);
        queryFormParams.put("busiId", "detailBill11");
        queryFormParams.put("operation", "query");
        queryFormParams.put("month", ms);
        queryFormParams.put("startDate", DateUtils.getFirstDayOfMonth(month, "d"));
        queryFormParams.put("endDate", DateUtils.getLastDayOfMonth(month, "d"));
        queryFormParams.put("detailType", type);
        queryFormParams.put("detailBillPwd", "undefined");
        queryFormParams.put("r", month);

        String url = "https://www.hn.10086.cn/ajax/billservice/detailBillInfo.jsp?1=1";
        url += HttpUtils.buildParamString(queryFormParams);
        try {
            HttpPost request = HttpUtils.post(url);
            CloseableHttpResponse response = client.execute(request);
            writeToFile(createTempFile(typeInFileName), response.getEntity());
            response.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean hasCaptcha() {
        return true;
    }

    @Override
    public File loadCaptchaCode() {
        try {
            cookieStore.addCookie(HttpUtils.getCookie("CmProvid", "hn", ".10086.cn", "/"));
            String url = "http://www.10086.cn";
            HttpUtils.executeGet(client, url);
            url = "http://www.10086.cn/hn";
            HttpUtils.executeGet(client, url);
            url = "https://hn.ac.10086.cn/login";
            HttpUtils.executeGet(client, url);
            url = "https://www.hn.10086.cn/login.jsp";
            // HttpUtils.executeGet(client, url);
            String responseString = HttpUtils.executeGetWithResult(client, url);
            // logger.debug(responseString);
            loginFormParams = HttpUtils.buildParamsFromHtml(responseString, "#loginForm");
            if (CollectionUtils.isEmpty(loginFormParams)) {
                return null;
            }
            HttpUtils.printCookies(cookieStore);
            url = "https://www.hn.10086.cn/login/isSsoLogin.jsp";
            HttpPost request = HttpUtils.post(url);
            addRequestHead(request);// 伪装ajax请求
            CloseableHttpResponse response = client.execute(request);
            responseString = EntityUtils.toString(response.getEntity());
            response.close();
            logger.debug(responseString);
            url = "https://www.hn.10086.cn/ajax/getAttachCodeKey.jsp";
            request = HttpUtils.post(url);
            addRequestHead(request);// 伪装ajax请求
            response = client.execute(request);
            responseString = EntityUtils.toString(response.getEntity());
            response.close();

            JSONObject json = JSON.parseObject(responseString);// {"serailNo":"20141218173129943312","succeed":"true"}
            if (!json.getBooleanValue("succeed")) {
                return null;
            }
            String serailNo = json.getString("serailNo");
            url = "https://www.hn.10086.cn/attachCode?serailNo=" + serailNo + "&" + System.currentTimeMillis();
            loginFormParams.put("serailNo", serailNo);
            return getCaptchaCodeImage(client, url);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private void addRequestHead(HttpRequest request) {
        request.addHeader("Origin", "https://www.hn.10086.cn");
        request.addHeader("Referer", "https://www.hn.10086.cn/login.jsp");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        request.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        request.addHeader("Accept-Language", "zh-CN,zh;q=0.8");
    }

    @Override
    public boolean checkCaptchaCode(String captchaCode) {
        return true;
    }

    @Override
    public boolean hasRandomcode() {
        return true;
    }

    @Override
    public boolean sendRandombySms() {
        try {
            String url = "https://www.hn.10086.cn/service/fee/detailBill.jsp";
            String resp = HttpUtils.executeGetWithResult(client, url);
            queryFormParams = HttpUtils.buildParamsFromHtml(resp, "#serviceForm");
            if (CollectionUtils.isEmpty(queryFormParams)) {
                return false;
            }
            url = "https://www.hn.10086.cn/ajax/checkHnMobileNumByDb.jsp?mobileNum=" + getPhone();
            HttpPost request = HttpUtils.post(url);
            request.removeHeaders("Referer");
            request.addHeader("Referer", "https://www.hn.10086.cn/service/fee/detailBill.jsp");
            resp = HttpUtils.executePostWithResult(client, request);
            logger.debug(resp.trim());
            url = "https://www.hn.10086.cn/ajax/pwdRadomSms.jsp?busiId=detailBill&mobileNum=" + getPhone();
            request = HttpUtils.post(url);
            addRequestHead(request);
            resp = HttpUtils.executePostWithResult(client, request).trim();
            logger.debug(resp);
            return resp.contains("随机短信密码已发送到");
        } catch (Exception e) {
            logger.error("发送短信失败", e);
        }
        return false;
    }

    @Override
    public boolean validateRandomcode(String randomCode) {
        String url = "https://www.hn.10086.cn/ajax/validateBusinessRandom.jsp?busiId=detailBill11&random=" + randomCode;
        try {
            String resp = HttpUtils.executePostWithResult(client, url, null).trim();
            if (!JSON.parseObject(resp).getBooleanValue("randomError")) {
                setRandomCode(randomCode);

                url = "https://www.hn.10086.cn/service/fee/detailBillInfo.jsp";
                queryFormParams.put("detailType", "1");
                queryFormParams.put("detailTypeSe", "语音详单_");
                queryFormParams.put("querymonth", DateUtils.formatDate(new Date(), PATTERN_10086));

                resp = HttpUtils.executePostWithResult(client, url, queryFormParams);
                queryFormParams = HttpUtils.buildHiddenInputParamsFromHtml(resp);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getIspSimpleName() {
        return ISP_CM;
    }

    @Override
    public String getAreaSimpleName() {
        return "hn";
    }

    private void logout() {
        try {
            logger.debug("退出登录");
            HttpUtils.executeGet(client, "https://www.hn.10086.cn/login/logout.jsp");
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
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
