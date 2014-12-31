package com.puhui.crawler.mobile;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
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
    private CloseableHttpClient client;
    private CloseableHttpClient client2;
    private static final String PATTERN_10086 = "yyyy-MM";
    private CookieStore cookieStore = new BasicCookieStore();
    private CookieStore cookieStore2 = new BasicCookieStore();

    public CT_HN_MobileFetcher() {
        this.client = HttpUtils.getHttpClient(false, cookieStore);
        this.client2 = HttpUtils.getHttpClient(false, cookieStore2);
    }

    @Override
    public File loadCaptchaCode() {
        String url = "http://hn.189.cn/hnwt/image/login/image.jsp?" + System.currentTimeMillis();
        return getCaptchaCodeImage(client, url);
    }

    @Override
    public boolean checkCaptchaCode(String captchaCode) {
        return true;
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
            String url = "http://hn.189.cn/hnselfservice/uamlogin/uam-login!validataLogin.action";
            Map<String, Object> params = new HashMap<>();
            // params.put("rUrl",
            // "/hnselfservice/billquery/bill-query!showTabs.action?_z=1");
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
            return responseString.contains("尊敬的中国电信客户") && responseString.contains("欢迎您登录网上营业厅");
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
            String url = "http://www.hn.189.cn/hnselfservice/billquery/bill-query!queryBillList.action?1=1";
            Map<String, Object> params = new HashMap<>();
            Date date = new Date();
            params.put("tm", date);
            params.put("tabIndex", "2");
            params.put("queryMonth", DateUtils.formatDate(date, PATTERN_10086));
            params.put("patitype", "2");
            params.put("valicode", "");
            params.put("accNbr", getPhone());
            params.put("chargeType", "10");
            params.put("_", System.currentTimeMillis());
            url += HttpUtils.buildParamString(params);
            String rs = HttpUtils.executeGetWithResult(client, url);
            logger.debug(rs);
            return rs.contains("请查收后填写随机密码");
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
        setRandomCode(randomCode);
        return true;
    }

    @Override
    public boolean loadBills() {
        try {
            hisBill();
            gsm();
            sms();
            gprs();
            addvalue();
            rc();
            personalInfo();
            accountBalance();
            address();
        } finally {
            this.close();
        }
        return true;
    }

    /**
     * 历史账单 从上月开始
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
        String ms = DateUtils.formatDate(month, "yyyyMM");
        logger.debug("bj.cdma历史账单：" + ms);
        String url = "http://www.hn.189.cn/hnselfservice/billquery/bill-query!queryUserBillDetail.action?1=1";
        Map<String, Object> params = new HashMap<>();
        params.put("chargeType", 10);
        params.put("queryMonth", month);
        params.put("productId", getPhone());
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
            commonFee(date, "2", BILL_TYPE_GSM, "通话详单");
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
        Map<String, Object> params = new HashMap<>();
        logger.debug(desc + ms);
        params.put("tm", DateUtils.add(month, Calendar.YEAR, 30));
        params.put("tabIndex", "2");
        params.put("queryMonth", ms);
        params.put("patitype", type);
        params.put("valicode", getRandomCode());
        params.put("accNbr", getPhone());
        params.put("chargeType", "10");
        params.put("_", System.currentTimeMillis());

        String url = "http://www.hn.189.cn/hnselfservice/billquery/bill-query!queryBillList.action?1=1";
        url += HttpUtils.buildParamString(params);
        try {
            HttpPost request = HttpUtils.post(url);
            CloseableHttpResponse response = client.execute(request);
            writeToFile(createTempFile(typeInFileName), response.getEntity());
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
        String url = "http://www.hn.189.cn/hnselfservice/customerinfomanager/customer-info!queryCustInfo.action";
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

    }

    @Override
    protected void addvalue() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            commonFee(date, "8", BILL_TYPE_ADDVALUE, "增值业务");
            date = DateUtils.addMonths(date, -1);
        }
    }

    @Override
    protected void rc() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            rc(date);
            date = DateUtils.addMonths(date, -1);
        }
    }

    private void rc(Date month) {
        try {
            String ms = DateUtils.formatDate(month, "yyyyMM");
            String url = "http://hn.189.cn/hnselfservice/billquery/bill-query!queryServDisctWT.action?tabIndex=4&queryMonth="
                    + ms + "&accNbr=" + getPhone() + "&chargeType=10&_=" + System.currentTimeMillis();
            try {
                HttpGet get = HttpUtils.get(url);
                CloseableHttpResponse response = client.execute(get);
                writeToFile(createTempFile(BILL_TYPE_RC), response.getEntity());
                response.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.error("查询套餐使用情况失败", e);
        }
    }

    @Override
    protected void gprs() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            commonFee(date, "9", BILL_TYPE_GPRS, "gprs");
            date = DateUtils.addMonths(date, -1);
        }
    }

    @Override
    protected void mon() {

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

    @Override
    protected void address() {
        try {
            copyCookie();
            String url = "http://www.189.cn/dqmh/productOnLine.do?method=login";
            HttpUtils.executeGet(client2, url);
            url = "http://www.189.cn/dqmh/userCenter/myOrderInfoList.do?method=mangeAddr&opt=init&type=outlink";
            HttpGet get = HttpUtils.get(url);
            get.addHeader("Referer", "http://hn.189.cn/jsp/service/order/addressmanger.jsp");
            CloseableHttpResponse response = client2.execute(get);
            writeToFile(createTempFile(BILL_TYPE_ADDRESS), response.getEntity());
            response.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void copyCookie() {
        for (Cookie cookie : cookieStore.getCookies()) {
            if (!cookie.getName().equals("JSESSIONID")) {
                cookieStore2.addCookie(cookie);
            }
        }
    }

    @Override
    protected void accountBalance() {
        String url = "http://hn.189.cn/hnselfservice/billquery/bill-query!queryBanlance.action?tabIndex=3&accNbr="
                + getPhone() + "&chargeType=10";
        try {
            HttpGet get = HttpUtils.get(url);
            CloseableHttpResponse response = client.execute(get);
            writeToFile(createTempFile(BILL_TYPE_ACCOUNTBALANCE), response.getEntity());
            response.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
