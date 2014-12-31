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
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.puhui.crawler.util.DateUtils;
import com.puhui.crawler.util.HttpUtils;
import com.puhui.crawler.util.SSLUtils;

/**
 * 北京移动
 * 
 * @author zhuyuhang
 */
public class CM_BJ_MobileFetcher extends MobileFetcher {
    private static final Logger logger = Logger.getLogger(CM_BJ_MobileFetcher.class);
    private String ssoSessionID;
    private CloseableHttpClient client;
    private CloseableHttpClient client2;
    private static String storePasswd = "123456";
    private static final String PATTERN_10086 = "yyyy.MM";
    private CookieStore cookieStore = new BasicCookieStore();
    private static SSLConnectionSocketFactory sscsf = SSLUtils.createSSLConnectionSocketFactory(
            CM_BJ_MobileFetcher.class.getResourceAsStream("/certs/cmodsvr1.bj.chinamobile.com.keystore"), storePasswd);

    public CM_BJ_MobileFetcher() {
        this.client = HttpUtils.getHttpClient(true, cookieStore);
        this.client2 = HttpUtils.getHttpClient(sscsf, cookieStore);
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
            return this.prepare() && this.bmccMobile() && this.isShowValidateRnum() && this.validateIp()
                    && this.rnumCheck() && this.login();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 预备起 主要是写cookie
     * 
     * @author zhuyuhang
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    private boolean prepare() {
        try {
            String url = "https://bj.ac.10086.cn/login";
            HttpGet get = HttpUtils.get(url);
            client.execute(get).close();

            url = "https://bj.ac.10086.cn/ac/cmsso/iloginnew.jsp";
            get = HttpUtils.get(url);
            client.execute(get).close();

            cookieStore.addCookie(new BasicClientCookie("c_mobile", this.getPhone()));
            cookieStore.addCookie(new BasicClientCookie("login_mobile", this.getPhone()));
            logger.debug("预备");
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 是否显示验证码
     * 
     * @author zhuyuhang
     * @throws IOException
     * @throws ClientProtocolException
     */
    private boolean isShowValidateRnum() {
        try {
            String url = "https://bj.ac.10086.cn/ac/IsShowValidateRnum";
            Map<String, Object> params = new HashMap<>();
            params.put("phone", this.getPhone());
            client.execute(HttpUtils.post(url, params)).close();
            logger.debug("是否显示验证码");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 验证BmccMobile
     * 
     * @author zhuyuhang
     * @throws Exception
     */
    private boolean bmccMobile() {
        try {
            String url = "https://bj.ac.10086.cn/ac/BmccMobile";
            Map<String, Object> params = new HashMap<>();
            params.put("mobile", this.getPhone());
            HttpPost post = HttpUtils.post(url, params);
            CloseableHttpResponse response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity()).trim();
            response.close();

            JSONObject json = JSON.parseObject(responseString);
            if (json.containsKey("BMCC_MOB") && "OK".equalsIgnoreCase(json.getString("BMCC_MOB"))) {
                logger.debug("验证手机号");
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
    private boolean validateIp() {
        try {
            String url = "https://bj.ac.10086.cn/ac/ValidateIp";
            CloseableHttpResponse response = client.execute(HttpUtils.post(url));
            String responseString = EntityUtils.toString(response.getEntity());
            response.close();
            JSONObject json = JSON.parseObject(responseString);
            if (json.containsKey("val-ip") && "ok".equalsIgnoreCase(json.getString("val-ip"))) {
                logger.debug("验证IP");
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
    private boolean rnumCheck() {
        try {
            // 验证验证码
            String url = "https://bj.ac.10086.cn/ac/ValidateRnum";
            Map<String, Object> params = new HashMap<>();
            params.put("user", this.getPhone());
            params.put("phone", this.getPhone());
            params.put("rnum", this.getCaptchaCode());
            params.put("service", "www.bj.10086.cn");
            // params.put("ssoLogin", "yes");
            params.put("loginMode", "3");
            params.put("loginMethod", "1");

            HttpPost post = HttpUtils.post(url, params);
            CloseableHttpResponse response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            response.close();
            JSONObject json = JSON.parseObject(responseString);
            if (json.containsKey("rnum-check") && "ok".equalsIgnoreCase(json.getString("rnum-check"))) {
                logger.debug("验证验证码");
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
    private boolean login() {
        try {
            String url = "https://bj.ac.10086.cn/ac/CmSsoLogin?1=1";
            Map<String, Object> params = new HashMap<>();
            params.put("user", this.getPhone());
            params.put("phone", this.getPhone());
            params.put("backurl", "http://www.bj.10086.cn/my");
            params.put("continue", "http://www.bj.10086.cn/my");
            params.put("style", "BIZ_LOGINBOX");
            params.put("service", "www.bj.10086.cn");
            params.put("box", null);
            params.put("target", "_parent");
            params.put("ssoLogin", "yes");
            params.put("loginMode", "3");
            params.put("loginMethod", "1");
            params.put("loginName", this.getPhone());
            params.put("password", this.getPassword());
            params.put("smsNum", "随机码");
            params.put("rnum", this.getCaptchaCode());
            params.put("ckCookie", "on");
            HttpPost post = HttpUtils.post(url, params);
            CloseableHttpResponse response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            logger.debug(responseString);
            response.close();

            // 是否已经单登录 如果是就顶掉继续登录
            post = HttpUtils.buildPostFromHtml(responseString, "#loginAgain div form");
            if (post != null) {
                client.execute(post).close();
            }

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
            logger.debug("bj10086登录，ssoSessionID:" + ssoSessionID);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 获取账单
     * 
     * @author zhuyuhang
     * @return
     */
    protected void submitBillTasks() {
        try {
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
                    loginSecondCheck();
                    super.submitBillTasks();
                    close();
                }
            }
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
    private void loginSecondCheck() {
        Date month = new Date();
        logger.debug("bj10086二次登录确认");
        String url = "https://cmodsvr1.bj.chinamobile.com/PortalCMOD/LoginSecondCheck?1=1";
        Map<String, Object> params = new HashMap<>();
        params.put("searchType", "HisDetail");
        params.put("checkMonth", DateUtils.formatDate(month, PATTERN_10086));
        params.put("detailType", "rc");
        params.put("password", this.getPassword());
        params.put("ssoSessionID", ssoSessionID);
        try {
            HttpUtils.executePost(client2, url, params);
        } catch (ClientProtocolException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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
        try {
            HttpGet get = HttpUtils.get(url);
            CloseableHttpResponse response = client2.execute(get);
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
        commonDetail(month, "gsm", "通话详单");
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
        commonDetail(month, "sms", "短信详单");
    }

    /**
     * 充值详单
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    protected void mzlog() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            mzlog(date);
            date = DateUtils.addMonths(date, -1);
        }
    }

    /**
     * 充值详单
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    private void mzlog(Date month) {
        commonDetail(month, "mzlog", "充值详单");
    }

    /**
     * 增值详单
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    protected void addvalue() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            addvalue(date);
            date = DateUtils.addMonths(date, -1);
        }
    }

    /**
     * 增值详单
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    private void addvalue(Date month) {
        commonDetail(month, "addvalue", "增值详单");
    }

    /**
     * 套餐及固定费
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    protected void rc() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            rc(date);
            date = DateUtils.addMonths(date, -1);
        }
    }

    /**
     * 套餐及固定费
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    private void rc(Date month) {
        commonDetail(month, "rc", "套餐及固定费");
    }

    /**
     * 上网流量
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    protected void gprs() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            gprs(date);
            date = DateUtils.addMonths(date, -1);
        }
    }

    /**
     * 上网流量
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    private void gprs(Date month) {
        commonDetail(month, "gprs", "gprs");
    }

    /**
     * 代收费用
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    protected void mon() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            mon(date);
            date = DateUtils.addMonths(date, -1);
        }
    }

    /**
     * 代收费用
     * 
     * @author zhuyuhang
     * @throws ClientProtocolException
     * @throws IOException
     */
    private void mon(Date month) {
        commonDetail(month, "mon", "代收费用");
    }

    /**
     * 通用详单查询方法 因为只有detailType在变
     * 
     * @author zhuyuhang
     * @param month
     * @param detailType
     * @param description
     * @throws ClientProtocolException
     * @throws IOException
     */
    private void commonDetail(Date month, String detailType, String description) {
        Map<String, Object> params = new HashMap<>();
        String ms = DateUtils.formatDate(month, PATTERN_10086);
        logger.debug(description + ms);
        params.put("checkMonth", ms);
        params.put("detailType", detailType);
        params.put("ssoSessionID", ssoSessionID);

        String url = "https://cmodsvr1.bj.chinamobile.com/PortalCMOD/detail/detail.do?1=1";
        url += HttpUtils.buildParamString(params);
        HttpGet get = HttpUtils.get(url);
        try {
            CloseableHttpResponse response = client2.execute(get);
            writeToFile(createTempFile(detailType), response.getEntity());
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
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
            if (this.client2 != null) {
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
        String url = "https://bj.ac.10086.cn/ac/ValidateNum?smartID=" + System.currentTimeMillis();
        return getCaptchaCodeImage(client, url);
    }

    @Override
    public boolean checkCaptchaCode(String captchaCode) {
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
        return "bj";
    }

    @Override
    protected void personalInfo() {

    }

    @Override
    protected void address() {
        logger.debug("获取收货地址信息");
        try {
            String url = "http://service.bj.10086.cn/member/showMember.action";
            String content = HttpUtils.executePostWithResult(client, url, null);
            writeToFile(createTempFile(BILL_TYPE_ADDRESS), content);
        } catch (Exception e) {
            logger.error("获取收货地址信息失败", e);
        }
    }

    @Override
    protected void accountBalance() {
        // TODO 解析需要参考js
        // 21.04err0.0err0.0err0.0err0.0 积分 65_65积分 套餐及固定费 28.00 套餐外语音通信费 3.30
        // 套餐外上网费 0.00 套餐外短信/彩信费 1.20 增值业务费 0.00 代收业务费 0.00 其他费用 0.00 优惠及减免 0.00
        // 合计 32.50
        try {
            String url = "http://www.bj.10086.cn/www/servletfuwuhfnew";
            String content = HttpUtils.executePostWithResult(client, url, null);
            writeToFile(createTempFile(BILL_TYPE_ACCOUNTBALANCE), content);
        } catch (Exception e) {
            logger.error("获取余额信息失败", e);
        }
    }

    @Override
    protected void currFee() {
        // TODO Auto-generated method stub

    }
}
