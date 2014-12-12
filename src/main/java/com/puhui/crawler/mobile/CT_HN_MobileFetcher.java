package com.puhui.crawler.mobile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.puhui.crawler.util.DateUtils;
import com.puhui.crawler.util.HttpUtils;

/**
 * 湖南电信
 * 
 * @author zhuyuhang
 */
public class CT_HN_MobileFetcher extends MobileFetcher {
    private Logger logger = Logger.getLogger(CT_HN_MobileFetcher.class);
    private String ssoSessionID;
    private CloseableHttpClient client;
    private static final String PATTERN_10086 = "yyyy.MM";
    private CookieStore cookieStore = new BasicCookieStore();

    public CT_HN_MobileFetcher() {
        this.client = HttpUtils.getHttpClient(false, cookieStore);
    }

    @Override
    public File loadCaptchaCode() {
        String url = "http://hn.189.cn/hnwt/image/login/image.jsp?" + System.currentTimeMillis();
        return getCaptchaCodeImage(client, url);
    }

    @Override
    public boolean checkCaptchaCode() {
        try {
            return true;
        } catch (Exception e) {
            logger.error("验证附加码错误", e);
        }
        return false;
    }

    @Override
    public boolean login(String phone, String password, String rnum) {
        super.login(phone, password, rnum);
        try {
            return this.prepare() && this.login();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 预备
     * 
     * @author zhuyuhang
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    private boolean prepare() throws ClientProtocolException, IOException {
        try {
            String url = "http://hn.189.cn/";
            HttpGet get = HttpUtils.get(url);
            client.execute(get).close();
            logger.debug("预备");
            url = "http://hn.189.cn/hnselfservice/uamlogin/uam-login!userLoginPage.action?modelPage=2&jsFunctionName=divCloseHead";
            HttpUtils.executePost(client, url);
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
    private boolean login() {
        try {
            cookieStore.addCookie(new BasicClientCookie("CNZZDATA4016530",
                    "alicnzz_eid%3D1677414815-1418353731-%26ntime%3D1418353731ve"));
            cookieStore.addCookie(new BasicClientCookie("IESESSION", "alive"));

            String url = "http://hn.189.cn/hnselfservice/uamlogin/uam-login!validataLogin.action";
            Map<String, Object> params = new HashMap<>();
            params.put("logonPattern", "2");
            params.put("userType", "2000004");
            params.put("productId", getPhone());
            params.put("loginPwdType", "01");
            params.put("userPwd", getPassword());
            params.put("validateCode", null);
            HttpPost post = HttpUtils.post(url, params);
            post.addHeader("Referer", "http://hn.189.cn/");
            String responseString = HttpUtils.executePostWithResult(client, post);
            logger.debug(responseString);
            JSONObject json = JSON.parseObject(responseString);
            String result = json.getString("result");
            if (!"success".equals(result)) {
                return false;
            }
            url = "http://202.103.124.44/LoginIn";
            params = new HashMap<>();
            params.put("SSORequestXML", json.get("paramUam"));
            params.put("submit", json.get("Submit Query"));
            post = HttpUtils.post(url, params);
            post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            post.setHeader("Accept-Language", "en-US,en;q=0.5");
            post.addHeader("Referer", "http://hn.189.cn/");
            post.addHeader("DNT", "1");

            CloseableHttpResponse response = client.execute(post);
            // http://hn.189.cn:80/hnselfservice/uamlogin/uam-login!uamLoginRet.action?UATicket=A71951038440184AF0AA9EEB259B88E5898B665374FA3231708E523BFD378BCD
            url = HttpUtils.getHeader(response, "Location");
            response.close();
            if (url == null) {
                return false;
            }
            if (url.split("UATicket=")[1].length() < 64) {
                return false;
            }
            HttpGet get = HttpUtils.get(url);
            response = client.execute(get);
            // http://hn.189.cn:80/hnselfservice/usercenter/user-center!userCenterIndex.action
            url = HttpUtils.getHeader(response, "Location");
            response.close();

            get = HttpUtils.get(url);
            response = client.execute(get);
            responseString = EntityUtils.toString(response.getEntity());
            response.close();
            logger.debug(responseString);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 发送随机验证码
     * 
     * @author zhuyuhang
     * @return
     */
    @Override
    public boolean sendRandombySms() {
        try {
            String url = "http://bj.189.cn/service/bill/validateRandomcode.action";
            String rs = HttpUtils.executePostWithResult(client, url, null);
            logger.debug(rs);
            JSONObject json = JSON.parseObject(rs);
            String tip = json.getString("tip");
            return StringUtils.isBlank(tip);
        } catch (Exception e) {
            logger.error("发送随机验证码失败", e);
        }
        return false;
    }

    /**
     * 验证随机短信码
     * 
     * @author zhuyuhang
     * @return
     */
    @Override
    public boolean validateRandomcode(String randomCode) {
        try {
            String url = "http://bj.189.cn/service/bill/billDetailQuery.action";
            Map<String, Object> params = new HashMap<>();
            params.put("requestFlag", "asynchronism");
            params.put("sRandomCode", randomCode);
            params.put("shijian", new Date());
            String rs = HttpUtils.executePostWithResult(client, url, params);
            logger.debug(rs);
            JSONObject json = JSON.parseObject(rs);
            return Boolean.valueOf(json.getString("billDetailValidate"));
        } catch (Exception e) {
            logger.error("验证随机验证码失败", e);
        }
        return false;
    }

    @Override
    public boolean loadBills() {
        this.submitBillTasks();// 获取账单
        return true;
    }

    /**
     * 获取账单
     * 
     * @author zhuyuhang
     * @return
     */
    protected void submitBillTasks() {
        try {
            super.submitBillTasks();
        } finally {
            this.close();
        }
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
            date = DateUtils.addMonths(date, -1);
            hisBill(date);
        }
    }

    /**
     * 历史账单
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    private void hisBill(Date month) {
        // http://bj.189.cn/service/bill/billInfoQuery.action?billReqType=3&billCycle=201406
        // 实时 get
        String ms = DateUtils.formatDate(month, "yyyyMM");
        logger.debug("bj.cdma历史账单：" + ms);
        String url = "http://bj.189.cn/service/bill/billInfoQuery.action?billReqType=3&billCycle=" + ms;
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
        logger.debug("bj.cdma通话详单" + ms);
        params.put("billDetailValidate", "true");
        params.put("productSpecID", null);
        params.put("downBillDetailType", 0);
        params.put("downStartTime", null);
        params.put("downEndTime", null);
        params.put("billDetailType", 1);
        params.put("startTime", DateUtils.getFirstDayOfMonth(month));
        params.put("endTime", DateUtils.getLastDayOfMonth(month));
        // http://bj.189.cn/service/bill/billDetailQuery.action?billDetailValidate=true&productSpecID=&downBillDetailType=0&downStartTime=&downEndTime=&billDetailType=1&startTime=2014-09-01&endTime=2014-09-30
        String url = "http://bj.189.cn/service/bill/billDetailQuery.action";
        HttpPost post = HttpUtils.post(url, params);
        try {
            CloseableHttpResponse response = client.execute(post);
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
        // https://cmodsvr1.bj.chinamobile.com/PortalCMOD/detail/detail.do?checkMonth=2014.10&detailType=sms&ssoSessionID=2c9d82fa477d6ea30149e59938ae0b0e
        Map<String, Object> params = new HashMap<>();
        String ms = DateUtils.formatDate(month, PATTERN_10086);
        logger.debug("bj.cdma短信详单" + ms);
        params.put("checkMonth", ms);
        params.put("detailType", "sms");
        params.put("ssoSessionID", ssoSessionID);

        String url = "https://cmodsvr1.bj.chinamobile.com/PortalCMOD/detail/detail.do?1=1";
        url += HttpUtils.buildParamString(params);
        try {
            HttpGet get = HttpUtils.get(url);
            CloseableHttpResponse response = client.execute(get);
            writeToFile(createTempFile(BILL_TYPE_SMS), response.getEntity());
            response.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
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
            if (this.client != null) {
                this.client.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean hasCaptcha() {
        return false;
    }

    @Override
    public boolean hasRandomcode() {
        return true;
    }

    @Override
    public String getIspSimpleName() {
        return ISP_CT;
    }

    @Override
    public String getAreaSimpleName() {
        return "hn";
    }

    @Override
    protected void personalInfo() {
        String url = "http://hn.189.cn/hnselfservice/customerinfomanager/customer-info!queryCustInfo.action";
        try {
            HttpGet get = HttpUtils.get(url);
            CloseableHttpResponse response = client.execute(get);
            writeToFile(createTempFile(BILL_TYPE_PERSONALINFO), response.getEntity());
            response.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
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
        String url = "http://hn.189.cn/hnselfservice/billquery/bill-query!queryRealTimeTelCharge.action?tabIndex=0&chargeType=10&accNbr="
                + getPhone();
        try {
            HttpGet get = HttpUtils.get(url);
            CloseableHttpResponse response = client.execute(get);
            writeToFile(createTempFile(BILL_TYPE_CURRFEE), response.getEntity());
            response.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
