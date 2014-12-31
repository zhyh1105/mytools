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

import com.puhui.crawler.util.DateUtils;
import com.puhui.crawler.util.HttpUtils;
import com.puhui.crawler.util.SSLUtils;

/**
 * 湖南电信
 * 
 * @author zhuyuhang
 */
public class CT_HN2_MobileFetcher extends MobileFetcher {
    private Logger logger = Logger.getLogger(CT_HN2_MobileFetcher.class);
    private CloseableHttpClient client;
    private CloseableHttpClient clientUamHbCt10000;
    private CloseableHttpClient clientWithSSL;
    private static final String PATTERN_10086 = "yyyyMM";
    private CookieStore cookieStore = new BasicCookieStore();
    private static SSLConnectionSocketFactory sscsf = SSLUtils
            .createSSLConnectionSocketFactory("/certs/uam.ct10000.com.keystore");
    private String cityCode = null;

    public CT_HN2_MobileFetcher() {
        this.client = HttpUtils.getHttpClient(false, cookieStore);
        this.clientUamHbCt10000 = HttpUtils.getHttpClient(false, null);
        this.clientWithSSL = HttpUtils.getHttpClient(sscsf, null);
    }

    @Override
    public File loadCaptchaCode() {
        String url = "https://uam.ct10000.com/ct10000uam/login?service=http://www.189.cn/dqmh/Uam.do?method=loginJTUamGet&returnURL=1&register=register2.0&UserIp=";
        try {
            HttpUtils.executeGet(clientWithSSL, url);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        url = "https://uam.ct10000.com/ct10000uam/validateImg.jsp?" + System.currentTimeMillis();
        return getCaptchaCodeImage(clientWithSSL, url);
    }

    @Override
    public boolean checkCaptchaCode(String captchaCode) {
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
        return prepare() && login();
    }

    private BasicClientCookie createCookie(final String name, final String value) {
        BasicClientCookie result = new BasicClientCookie(name, value);
        result.setDomain(".189.cn");
        result.setPath("/");
        return result;
    }

    /**
     * 预备
     * 
     * @author zhuyuhang
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    private boolean prepare() {
        try {
            logger.debug("预备");
            String url = "http://www.189.cn/hn/";
            HttpUtils.executeGet(client, url);
            cookieStore.addCookie(createCookie("cityCode", "hb"));
            cookieStore.addCookie(createCookie("SHOPID_COOKIEID", "10019"));

            url = "http://www.189.cn/dqmh/cms/index/login_jx.jsp?shopId=10019&rand=" + System.currentTimeMillis();
            HttpUtils.executeGet(client, url);

            url = "http://www.189.cn/dqmh/productOnLine.do?method=login";
            HttpUtils.executePost(client, url);

            url = "http://www.189.cn/dqmh/Uam.do?method=getUamUserInfo";
            HttpUtils.executeGet(client, url);

            url = "http://www.189.cn/dqmh/login/loginJT.jsp?1=1";
            Map<String, Object> params = new HashMap<>();
            params.put("UserUrlto", "/dqmh/frontLink.do?method=linkTo");
            params.put("shopId", "10019");
            params.put(
                    "toStUrl",
                    "http://hn.189.cn:80/hnselfservice/uamlogin/uam-login!uamLoginRet.action?rUrl=/hnselfservice/usercenter/user-center!userCenterIndex.action?_z=1");
            HttpUtils.executeGet(client, url);

            url = "http://www.189.cn/dqmh/login/loginJT.jsp?1=1";
            params = new HashMap<>();
            params.put("UserUrlto", "/dqmh/frontLink.do?method=linkTo");
            params.put("shopId", "10019");
            params.put("toStUrl", "toStUrl");
            url += HttpUtils.buildParamString(params);
            HttpUtils.executeGet(client, url);

            url = "http://www.189.cn/dqmh/userCenter/yiJianLoginAction.do?method=toLoginJT&shopId=10019";
            HttpUtils.executeGet(client, url);
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
    @SuppressWarnings("unchecked")
    private boolean login() {
        try {
            Map<String, Object> params = new HashMap<>();
            String url = "http://www.189.cn/dqmh/Uam.do?1=1";
            params = new HashMap<>();
            params.put("method", "loginUamSendJT");
            params.put("logintype", "telephone");
            params.put("shopId", "10019");
            params.put("loginRequestURLMark",
                    "http://www.189.cn/dqmh/login/loginJT.jsp?UserUrlto=/dqmh/frontLink.do?method=linkTo");
            params.put(
                    "toStUrl",
                    "http://hn.189.cn:80/hnselfservice/uamlogin/uam-login!uamLoginRet.action?rUrl=/hnselfservice/usercenter/user-center!userCenterIndex.action?_z=1");
            params.put("date", System.currentTimeMillis());
            url += HttpUtils.buildParamString(params);
            HttpGet get = HttpUtils.get(url);
            get.addHeader(
                    "Referer",
                    "http://www.189.cn/dqmh/login/loginJT.jsp?UserUrlto=/dqmh/frontLink.do?method=linkTo&shopId=10019&toStUrl=http://hn.189.cn:80/hnselfservice/uamlogin/uam-login!uamLoginRet.action?rUrl=/hnselfservice/usercenter/user-center!userCenterIndex.action?_z=1");
            CloseableHttpResponse response = client.execute(get);

            // 登录页面
            url = "https://uam.ct10000.com/ct10000uam/login?service=http://www.189.cn/dqmh/Uam.do?method=loginJTUamGet&returnURL=1&register=register2.0&UserIp=";
            get = HttpUtils.get(url);
            response = clientWithSSL.execute(get);
            String responseString = EntityUtils.toString(response.getEntity());
            response.close();
            logger.debug(responseString);
            Map<String, Object> urlAndParams = HttpUtils.getFormUrlAndParamsFromHtml(responseString, "#c2000004");
            params = (Map<String, Object>) urlAndParams.get("params");
            params.put("forbidpass", "null");
            params.put("forbidaccounts", "null");
            params.put("authtype", "c2000004");
            params.put("customFileld02", "19");
            params.put("areaname", "湖南");
            params.put("username", getPhone());
            params.put("customFileld01", "3");
            params.put("password", getPassword());
            params.put("randomId", getCaptchaCode());
            params.put("open_no", "c2000004");
            HttpPost post = HttpUtils.post(url, params);
            post.setHeader("Origin", "https://uam.ct10000.com");
            post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            post.setHeader("Accept-Language", "CN,zh;q=0.8");
            post.setHeader("Cache-Control", "max-age=0");
            post.setHeader(
                    "Referer",
                    "https://uam.ct10000.com/ct10000uam/login?service=http://www.189.cn/dqmh/Uam.do?method=loginJTUamGet&returnURL=1&register=register2.0&UserIp=");
            // 登录请求
            response = clientWithSSL.execute(post);
            url = HttpUtils.getLocationFromHeader(response, true);// https://uam.ct10000.com:443/ct10000uam-gate/?SSORequestXMLRETURN=http://www.189.cn/dqmh/Uam.do?method=loginJTUamGet&UATicket=35nullST--1576939-2nxGBmGCKhCfvX9K1zhF-ct10000uam
            logger.debug(url);
            String uATicket = url.split("UATicket=")[1];
            responseString = HttpUtils.executeGetWithResult(clientWithSSL, url);
            logger.debug(responseString);
            // TODO
            url = "http://www.189.cn/dqmh/Uam.do?method=loginJTUamGet&UATicket=" + uATicket;
            get = HttpUtils.get(url);
            response = client.execute(get);
            response.close();
            url = "http://www.189.cn/dqmh/frontLink.do?1=1";
            params = new HashMap<>();
            params.put("method", "linkTo");
            params.put("shopId", "10019");
            params.put(
                    "toStUrl",
                    "http://hn.189.cn:80/hnselfservice/uamlogin/uam-login!uamLoginRet.action?rUrl=/hnselfservice/usercenter/user-center!userCenterIndex.action?_z=1");
            url += HttpUtils.buildParamString(params);
            get = HttpUtils.get(url);
            response = client.execute(get);

            url = HttpUtils.getLocationFromHeader(response, true);// https://uam.ct10000.com/ct10000uam-gate/SSOFromUAM?ReturnURL=687474703A2F2F686E2E3138392E636E3A38302F686E73656C66736572766963652F75616D6C6F67696E2F75616D2D6C6F67696E2175616D4C6F67696E5265742E616374696F6E3F7255726C3D2F686E73656C66736572766963652F7573657263656E7465722F757365722D63656E746572217573657243656E746572496E6465782E616374696F6E3F5F7A3D31&ProvinceId=19
            get = HttpUtils.get(url);
            response = clientWithSSL.execute(get);

            url = HttpUtils.getLocationFromHeader(response, true); // https://uam.ct10000.com/ct10000uam/login?service=https%3A%2F%2Fuam.ct10000.com%3A443%2Fct10000uam-gate%2FSSOFromUAM%3FReturnURL%3D687474703A2F2F686E2E3138392E636E3A38302F686E73656C66736572766963652F75616D6C6F67696E2F75616D2D6C6F67696E2175616D4C6F67696E5265742E616374696F6E3F7255726C3D2F686E73656C66736572766963652F7573657263656E7465722F757365722D63656E746572217573657243656E746572496E6465782E616374696F6E3F5F7A3D31%26ProvinceId%3D19&serviceId=001&ProvinceId=19
            get = HttpUtils.get(url);
            response = clientWithSSL.execute(get);

            url = HttpUtils.getLocationFromHeader(response, true);// https://uam.ct10000.com/ct10000uam-gate/?SSORequestXMLRETURN=https://uam.ct10000.com:443/ct10000uam-gate/SSOFromUAM?ReturnURL=687474703A2F2F686E2E3138392E636E3A38302F686E73656C66736572766963652F75616D6C6F67696E2F75616D2D6C6F67696E2175616D4C6F67696E5265742E616374696F6E3F7255726C3D2F686E73656C66736572766963652F7573657263656E7465722F757365722D63656E746572217573657243656E746572496E6465782E616374696F6E3F5F7A3D31&ProvinceId=19&ProvinceUrl=687474703A2F2F3230322E3130332E3132342E34342F53534F46726F6D435455414D&UATicket=35001ST--1576945-jOAks7KZuSkG2Z3KxtB9-ct10000uam

            get = HttpUtils.get(url);
            response = clientWithSSL.execute(get);
            response.close();

            uATicket = url.split("UATicket=")[1];
            url = "http://202.103.124.44/SSOFromCTUAM?UATicket=" + uATicket;
            get = HttpUtils.get(url);
            response = clientUamHbCt10000.execute(get);
            url = HttpUtils.getLocationFromHeader(response, true);// http://hn.189.cn/hnselfservice/uamlogin/uam-login!uamLoginRet.action?rUrl=/hnselfservice/usercenter/user-center!userCenterIndex.action?_z=1&UATicket=30EDA225912DED136B7E0F9F7031ED782A28FD036A3AE06788180DB3CB7531C9
            get = HttpUtils.get(url);
            response = client.execute(get);
            url = HttpUtils.getLocationFromHeader(response, true);// http://hn.189.cn/hnselfservice/usercenter/user-center!userCenterIndex.action?_z=1
            get = HttpUtils.get(url);
            response = client.execute(get);
            url = "http://hn.189.cn/hnselfservice/business/billing-deal!getProdListByType.action?id="
                    + +System.currentTimeMillis() + "&action=/hnselfservice/usercenter/user-center!dataLoading.action";
            get = HttpUtils.get(url);
            response = client.execute(get);
            responseString = EntityUtils.toString(response.getEntity());
            logger.debug(responseString);
            response.close();
            if (responseString.contains(getPhone() + "(移动电话)")) {
                logger.debug("登录成功");
            } else {
                return false;
            }
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
            String url = "http://hb.189.cn/feesquery_sentPwd.action";
            Map<String, Object> params = new HashMap<>();
            params.put("productNumber", getPhone());
            params.put("cityCode", this.cityCode);
            params.put("sentType", "C");
            params.put("ip", "0");
            String rs = HttpUtils.executePostWithResult(client, url, params).trim();
            logger.debug(rs);
            return (rs.indexOf("随机验证码已经发送到") > -1);
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
            String url = "http://hb.189.cn/feesquery_checkCDMAFindWeb.action";
            Map<String, Object> params = new HashMap<>();
            params.put("sentType", "C");
            params.put("random", randomCode);
            String rs = HttpUtils.executePostWithResult(client, url, params).trim();
            return "1".equals(rs);
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
        // TODO 现在业面无法展示
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
        commonFee(month, "1", BILL_TYPE_GSM, "语音详单");
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
            commonFee(date, "3", BILL_TYPE_SMS, "短信详单");
            date = DateUtils.addMonths(date, -1);
        }
    }

    /**
     * @author zhuyuhang
     * @param month
     *            月份
     * @param type
     *            详单类型 1 语音详单 3短信详单
     * @param billType
     * @see {@link MobileFetcher} BILL_TYPE_
     * @param desc
     */
    private void commonFee(Date month, String type, String billType, String desc) {
        Map<String, Object> params = new HashMap<>();
        String ms = DateUtils.formatDate(month, PATTERN_10086) + "0000";
        logger.debug(desc + ms);
        params.put("startMonth", ms);
        params.put("type", type);
        params.put("prod_type", "1");
        params.put("pagecount", CU_BJ_MobileFetcher.PAGE_SIZE);
        String url = "http://hb.189.cn/feesquery_querylist.action";
        try {
            HttpPost request = HttpUtils.post(url, params);
            CloseableHttpResponse response = client.execute(request);
            writeToFile(createTempFile(billType), response.getEntity());
            response.close();
        } catch (Exception e) {
            logger.error("获取[" + desc + "]失败", e);
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
            if (client != null) {
                client.close();
            }
            if (clientUamHbCt10000 != null) {
                clientUamHbCt10000.close();
            }
            if (clientWithSSL != null) {
                clientWithSSL.close();
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean hasCaptcha() {
        return true;
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
        String url = "http://hb.189.cn/pages/selfservice/custinfo/userinfo/userInfo.action";
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
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            commonFee(date, "4", BILL_TYPE_ADDVALUE, "增值业务");
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
            commonFee(date, "2", BILL_TYPE_GPRS, "上网流量");
            date = DateUtils.addMonths(date, -1);
        }
    }

    @Override
    protected void mon() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void currFee() {
        String url = "http://hb.189.cn/queryFeesYue.action";
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
