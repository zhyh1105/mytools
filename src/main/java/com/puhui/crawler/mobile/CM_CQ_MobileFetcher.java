package com.puhui.crawler.mobile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
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
 * 重庆移动
 * 
 * @author zhuyuhang
 */
public class CM_CQ_MobileFetcher extends MobileFetcher {
    private Logger logger = Logger.getLogger(CM_CQ_MobileFetcher.class);
    private CloseableHttpClient client;
    private CloseableHttpClient clientWithSSL;
    private static String storePasswd = "123456";
    private static final String PATTERN_10086 = "yyyyMM";
    private CookieStore cookieStore = new BasicCookieStore();
    private Map<String, Object> form1Params = null;
    private static final String FORM1_SELECTOR = "#Form1";

    private static SSLConnectionSocketFactory sscsf = SSLUtils.createSSLConnectionSocketFactory(
            CM_HB_MobileFetcher.class.getResourceAsStream("/certs/service.cq.10086.cn.keystore"), storePasswd);

    public CM_CQ_MobileFetcher() {
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
            String url = "http://www.cq.10086.cn";
            HttpUtils.executeGet(client, url);
            url = "https://cq.ac.10086.cn/login";
            HttpUtils.executeGet(client, url);
            url = "http://service.cq.10086.cn/app?service=page/newLogin.login";
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
            String url = "https://service.cq.10086.cn/app?service=page/newLogin.login&listener=login";
            Map<String, Object> params = new HashMap<>();
            params.put("service", "direct/1/newLogin.login/$Form");
            params.put("Form0", "S0");
            params.put("Form0", "Form0");
            params.put("USER_PASSWD_SELECT", "1");
            params.put("SERIAL_NUMBER", getPhone());
            // base64编码
            params.put("USER_PASSWD", Base64.encodeBase64String(getPassword().getBytes()));
            params.put("USER_PASSSMS", null);
            params.put("EFFICACY_CODE", getCaptchaCode());
            params.put("clogin", "on");
            HttpPost post = HttpUtils.post(url, params);
            CloseableHttpResponse response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            logger.debug(responseString);
            url = HttpUtils.getHeader(response, "Location");
            if (url == null) {
                return false;
            }
            response.close();
            responseString = HttpUtils.executeGetWithResult(client, url);
            logger.debug(responseString);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    protected void submitBillTasks() {
        try {
            // 二次登录确认
            this.loginSecondCheck();
            // 重庆移动只能线性请求 它有 token 验证
            // super.submitBillTasks();
            gsm();
            sms();
            hisBill();
            personalInfo();
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
        String url = "http://service.cq.10086.cn/app?service=page/personalinfo.SecondCheck&listener=doCheckedSms&PageNumber="
                + getRandomCode();
        HttpGet get = HttpUtils.get(url);
        CloseableHttpResponse response = client.execute(get);
        url = HttpUtils.getHeader(response, "Location");
        response.close();
        get = HttpUtils.get(url);
        response = client.execute(get);
        String responseString = EntityUtils.toString(response.getEntity());
        response.close();
        form1Params = HttpUtils.buildParamsFromHtml(responseString, FORM1_SELECTOR);
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
        Map<String, Object> params = new HashMap<String, Object>();
        String url = "http://service.cq.10086.cn/app?service=page/operation.AJAXMyBill&listener=makePieChar&qtime="
                + System.currentTimeMillis() + "&month=" + ms;
        url += HttpUtils.buildParamString(params);
        HttpGet get = HttpUtils.get(url);
        try {
            CloseableHttpResponse response = client.execute(get);
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
            commonFee(date, "10", BILL_TYPE_GSM, "通话详单");
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
            commonFee(date, "12", BILL_TYPE_SMS, "短信详单");
            date = DateUtils.addMonths(date, -1);
        }
    }

    @Override
    protected void personalInfo() {
        String url = "https://service.cq.10086.cn/app?service=page/operation.PersonalInfo&listener=initPage";
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
        // TODO Auto-generated method stub
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            // commonFee(date, "13", BILL_TYPE_ADDVALUE, "增值业务扣费记录");
            date = DateUtils.addMonths(date, -1);
        }
    }

    @Override
    protected void rc() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            // commonFee(date, "9", BILL_TYPE_RC, "套餐及固定费详单");
            date = DateUtils.addMonths(date, -1);
        }
    }

    @Override
    protected void gprs() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            // commonFee(date, "11", BILL_TYPE_GPRS, "上网详单");
            date = DateUtils.addMonths(date, -1);
        }
    }

    @Override
    protected void mon() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            // commonFee(date, "14", BILL_TYPE_MON, "代收业务扣费记录");
            date = DateUtils.addMonths(date, -1);
        }
    }

    @Override
    protected void currFee() {
        // TODO Auto-generated method stub

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
        if (form1Params == null || form1Params.isEmpty()) {
            return;
        }
        String ms = DateUtils.formatDate(month, PATTERN_10086);
        logger.debug(desc + ms);
        form1Params.put("QUERY_TYPE", "2");
        form1Params.put("SELECT_MONTH", ms);
        form1Params.put("infoType", type);
        form1Params.put("STARTDATE", "");
        form1Params.put("ENDDATE", "");
        form1Params.put("SELECT_MONTHVAl", getMonthVal());
        String url = "http://service.cq.10086.cn/app?service=page/myYD.OrderQuery&listener=detailQuery";
        try {
            HttpPost post = HttpUtils.post(url, form1Params);
            post.setHeader("Referer", "http://service.cq.10086.cn/app?service=page/myYD.OrderQuery&listener=initPage");
            CloseableHttpResponse response = client.execute(post);
            String contentString = writeToFile(createTempFile(typeInFileName), response.getEntity());
            response.close();
            form1Params.clear();
            form1Params = HttpUtils.buildParamsFromHtml(contentString, FORM1_SELECTOR);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private List<String> getMonthVal() {
        List<String> monthVal = new ArrayList<>();
        Date date = new Date();
        for (int i = 0; i < 6; i++) {
            monthVal.add(DateUtils.formatDate(date, PATTERN_10086));
            date = DateUtils.addMonths(date, -1);
        }
        return monthVal;
    }

    @Override
    public boolean hasCaptcha() {
        return true;
    }

    @Override
    public File loadCaptchaCode() {
        String url = "http://service.cq.10086.cn/icsimage?mode=validate&width=51&height=20&temp="
                + System.currentTimeMillis();
        return getCaptchaCodeImage(client, url);
    }

    @Override
    public boolean checkCaptchaCode() {
        return true;
    }

    @Override
    public boolean hasRandomcode() {
        return true;
    }

    @Override
    public boolean sendRandombySms() {
        String url = "https://service.cq.10086.cn/app?service=page/myYD.OrderQuery&listener=initPage";
        try {
            HttpGet get = HttpUtils.get(url);
            get.setHeader("Referer",
                    "http://service.cq.10086.cn/app?service=page/operation.myMobileBill&listener=initPage");
            CloseableHttpResponse response = clientWithSSL.execute(HttpUtils.get(url));
            url = HttpUtils.getHeader(response, "Location");
            logger.debug(url);
            response.close();
            get = HttpUtils.get(url);
            String responeString = HttpUtils.executeGetWithResult(client, url);
            logger.debug(responeString);
            return true;
        } catch (Exception e) {
            logger.error("发送短信失败", e);
        }
        return false;
    }

    @Override
    public boolean validateRandomcode(String randomCode) {
        // 这个不需要验证 直接传送过去
        setRandomCode(randomCode);
        return true;
    }

    @Override
    public String getIspSimpleName() {
        return ISP_CM;
    }

    @Override
    public String getAreaSimpleName() {
        return "cq";
    }

    private void logout() {
        try {
            logger.debug("退出登录");
            // HttpUtils.executeGet(client,
            // "http://service.cq.10086.cn/app?service=page/Home&listener=exit");
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
