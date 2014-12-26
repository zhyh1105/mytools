package com.puhui.crawler.mobile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
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
 * 重庆电信
 * 
 * @author zhuyuhang
 */
public class CT_CQ_MobileFetcher extends MobileFetcher {
    private Logger logger = Logger.getLogger(CT_CQ_MobileFetcher.class);
    private CloseableHttpClient client;
    private CloseableHttpClient clientUamCqCt10000;
    private CloseableHttpClient clientWithSSL;
    private CloseableHttpClient clientCqCt10000;
    private static String storePasswd = "123456";
    private static final String PATTERN_10086 = "yyyy-MM";
    private CookieStore cookieStore = new BasicCookieStore();
    private String ecsc_session_uuid = null;
    private String scriptSessionId = null;
    private String jsessionid_app3 = null;
    private String jsessionid_app4 = null;
    private int batchId = 0;
    private static SSLConnectionSocketFactory sscsf = SSLUtils.createSSLConnectionSocketFactory(
            CM_HB_MobileFetcher.class.getResourceAsStream("/certs/service.cq.10086.cn.keystore"), storePasswd);

    public CT_CQ_MobileFetcher() {
        this.client = HttpUtils.getHttpClient(false, cookieStore);
        this.clientCqCt10000 = HttpUtils.getHttpClient(false, null);
        this.clientUamCqCt10000 = HttpUtils.getHttpClient(false, null);
        this.clientWithSSL = HttpUtils.getHttpClient(sscsf, null);
    }

    @Override
    public File loadCaptchaCode() {
        String url = "https://uam.ct10000.com/ct10000uam/login?service=http://www.189.cn/dqmh/Uam.do?method=loginJTUamGet&returnURL=1&register=register2.0";
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

            String url = "http://www.189.cn/cq";
            HttpUtils.executeGet(client, url);

            cookieStore.addCookie(createCookie("cityCode", "cq"));
            cookieStore.addCookie(createCookie("SHOPID_COOKIEID", "10004"));
            // cookieStore.addCookie(createCookie("hasshown", "1"));
            // cookieStore.addCookie(createCookie("loginStatus",
            // "non-logined"));
            url = "http://www.189.cn/dqmh/cms/index/login_jx.jsp?rand=" + System.currentTimeMillis();
            HttpUtils.executeGet(client, url);

            url = "http://cq.189.cn/pay/www189cncq.htm";
            HttpUtils.executeGet(client, url);
            // ecsc_session_uuid=5fb08568377e422ea821539b51e450af:1419216852812
            this.ecsc_session_uuid = HttpUtils.getFirstCookie(cookieStore, "ecsc_session_uuid");
            // url =
            // "http://www.189.cn/dqmh/system.do?operate=getCookie&city=cq";
            // HttpPost post = HttpUtils.post(url);
            // CloseableHttpResponse response = client.execute(post);
            // url = HttpUtils.getLocationFromHeader(response);
            // response.close();
            // HttpUtils.executeGet(client, url);
            url = "http://www.189.cn/dqmh/Uam.do?method=getUamUserInfo";
            HttpGet get = HttpUtils.get(url);
            get.addHeader("Referer", "http://www.189.cn/dqmh/cms/index/login_jx.jsp?rand=1419216850902");
            HttpUtils.executeGet(client, url);

            url = "http://zxkf.cq.ct10000.com:8080/statistic/BrowserInfoServlet?1=1";
            Map<String, Object> params = new HashMap<>();
            params.put("sessionId", this.ecsc_session_uuid);
            params.put("lastUrl", "http://www.189.cn/cq/");
            params.put("screen", "1920*1080");
            params.put("acceptChannel", "2");
            url += HttpUtils.buildParamString(params);
            HttpUtils.executeGet(clientCqCt10000, url);

            url = "http://zxkf.cq.ct10000.com:8080/statistic/VisitTrackServlet?1=1";
            params = new HashMap<>();
            params.put("sessionId", this.ecsc_session_uuid);
            params.put("lastUrl", "http://www.189.cn/cq/");
            params.put("acceptChannel", "2");
            url += HttpUtils.buildParamString(params);
            HttpUtils.executeGet(clientCqCt10000, url);

            // url = "http://cq.189.cn/pages/login/sypay_group.jsp";
            // HttpUtils.executeGet(client, url);
            url = "http://www.189.cn/dqmh/login/loginJT.jsp?1=1";
            params = new HashMap<>();
            params.put("UserUrlto", "/dqmh/frontLink.do?method=linkTo");
            params.put("shopId", "10004");
            params.put("toStUrl",
                    "http://cq.189.cn/users/getTicket.htm?sendredirect=http://cq.189.cn/account/index.htm");
            url += HttpUtils.buildParamString(params);
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
            params.put("shopId", "10004");
            params.put("loginRequestURLMark",
                    "http://www.189.cn/dqmh/login/loginJT.jsp?UserUrlto=/dqmh/frontLink.do?method=linkTo");
            params.put("toStUrl",
                    "http://cq.189.cn/users/getTicket.htm?sendredirect=http://cq.189.cn/account/index.htm");
            params.put("date", System.currentTimeMillis());
            url += HttpUtils.buildParamString(params);
            HttpGet get = HttpUtils.get(url);
            get.addHeader(
                    "Referer",
                    "http://www.189.cn/dqmh/login/loginJT.jsp?UserUrlto=/dqmh/frontLink.do?method=linkTo&shopId=10004&toStUrl=http://cq.189.cn/users/getTicket.htm?sendredirect=http://cq.189.cn/account/index.htm");
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
            params.put("customFileld02", "04");
            params.put("areaname", "重庆");
            params.put("username", getPhone());
            params.put("c2000004RmbMe", "on");
            params.put("customFileld01", "1");
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
            url = HttpUtils.getLocationFromHeader(response, true);// https://uam.ct10000.com/ct10000uam-gate/?SSORequestXMLRETURN=http://www.189.cn/dqmh/Uam.do?method=loginJTUamGet&UATicket=35nullST--1188296-2K9ATnYKsDcUHMF35o7F-ct10000uam
            logger.debug(url);
            String uATicket = url.split("UATicket=")[1];
            responseString = HttpUtils.executeGetWithResult(clientWithSSL, url);
            logger.debug(responseString);
            url = "http://www.189.cn/dqmh/Uam.do?method=loginJTUamGet&UATicket=" + uATicket;
            get = HttpUtils.get(url);
            response = client.execute(get);
            response.close();
            url = "http://www.189.cn/dqmh/frontLink.do?method=linkTo&shopId=10004&toStUrl=http://cq.189.cn/users/getTicket.htm?sendredirect=http://cq.189.cn/account/index.htm";
            params = new HashMap<>();
            params.put("method", "linkTo");
            params.put("shopId", "10004");
            params.put("toStUrl",
                    "http://cq.189.cn/users/getTicket.htm?sendredirect=http://cq.189.cn/account/index.htm");
            get = HttpUtils.get(url);
            get.addHeader(
                    "Referer",
                    "http://www.189.cn/dqmh/login/loginJT.jsp?UserUrlto=/dqmh/frontLink.do?method=linkTo&amp;shopId=10004&amp;toStUrl=http://cq.189.cn/users/getTicket.htm?sendredirect=http://cq.189.cn/account/index.htm");
            response = client.execute(get);

            url = HttpUtils.getLocationFromHeader(response, true);// https://uam.ct10000.com/ct10000uam-gate/SSOFromUAM?ReturnURL=687474703A2F2F63712E3138392E636E2F75736572732F6765745469636B65742E68746D3F73656E6472656469726563743D687474703A2F2F63712E3138392E636E2F6163636F756E742F696E6465782E68746D&ProvinceId=04
            get = HttpUtils.get(url);
            get.addHeader(
                    "Referer",
                    "http://www.189.cn/dqmh/login/loginJT.jsp?UserUrlto=/dqmh/frontLink.do?method=linkTo&shopId=10004&toStUrl=http://cq.189.cn/users/getTicket.htm?sendredirect=http://cq.189.cn/account/index.htm");
            response = clientWithSSL.execute(get);

            url = HttpUtils.getLocationFromHeader(response, true); // https://uam.ct10000.com/ct10000uam/login?service=https%3A%2F%2Fuam.ct10000.com%3A443%2Fct10000uam-gate%2FSSOFromUAM%3FReturnURL%3D687474703A2F2F63712E3138392E636E2F75736572732F6765745469636B65742E68746D3F73656E6472656469726563743D687474703A2F2F63712E3138392E636E2F6163636F756E742F696E6465782E68746D%26ProvinceId%3D04&serviceId=001&ProvinceId=04
            get = HttpUtils.get(url);
            get.addHeader(
                    "Referer",
                    "http://www.189.cn/dqmh/login/loginJT.jsp?UserUrlto=/dqmh/frontLink.do?method=linkTo&shopId=10004&toStUrl=http://cq.189.cn/users/getTicket.htm?sendredirect=http://cq.189.cn/account/index.htm");
            response = clientWithSSL.execute(get);

            url = HttpUtils.getLocationFromHeader(response, true);// https://uam.ct10000.com/ct10000uam-gate/?SSORequestXMLRETURN=https://uam.ct10000.com:443/ct10000uam-gate/SSOFromUAM?ReturnURL=687474703A2F2F63712E3138392E636E2F75736572732F6765745469636B65742E68746D3F73656E6472656469726563743D687474703A2F2F63712E3138392E636E2F6163636F756E742F696E6465782E68746D&ProvinceId=04&ProvinceUrl=687474703A2F2F3232322E3137372E342E3130312F55415765622F736572766C65742F53534F46726F6D435455414D&UATicket=35001ST--1188298-QFosokXmJEEMgFBtvV1t-ct10000uam
            get = HttpUtils.get(url);
            get.addHeader(
                    "Referer",
                    "http://www.189.cn/dqmh/login/loginJT.jsp?UserUrlto=/dqmh/frontLink.do?method=linkTo&shopId=10004&toStUrl=http://cq.189.cn/users/getTicket.htm?sendredirect=http://cq.189.cn/account/index.htm");
            response = clientWithSSL.execute(get);
            response.close();

            uATicket = url.split("UATicket=")[1];
            url = "http://222.177.4.101/UAWeb/servlet/SSOFromCTUAM?UATicket=" + uATicket;
            get = HttpUtils.get(url);
            response = clientWithSSL.execute(get);
            // 下面提的这个 url 可能会有错误
            url = HttpUtils.getLocationFromHeader(response, true);// http://cq.189.cn/users/getTicket.htm?sendredirect=http%3A%2F%2Fcq.189.cn%2Faccount%2Findex.htm&UATicket=047611685
            uATicket = url.split("UATicket=")[1];
            logger.debug("url");
            get = HttpUtils.get(url);
            response = client.execute(get);
            responseString = EntityUtils.toString(response.getEntity());
            response.close();
            post = HttpUtils.buildPostFromHtml(responseString, "#frm");// http://cq.189.cn/sso/login?get-lt=true&method=direct&key=3133333638303338373538235F23313131&enc=386f06e57b89fe60eb281203bce885ce&service=http%3A%2F%2Fcq.189.cn%2Faccount%2Findex.htm
            response = client.execute(post);
            url = HttpUtils.getLocationFromHeader(response, true);// http:cq.189.cn/sso/login?lt=_cD2BDBC8C-94E2-BEFF-BF3A-320B73BCE091_k91838E27-6A24-E319-E9AD-8FEA06D612F3&_eventId=submit&method=direct&key=3133333638303338373538235F23313131&enc=386f06e57b89fe60eb281203bce885ce&service=http://cq.189.cn/account/index.htm
            get = HttpUtils.get(url);
            response = client.execute(get);

            url = HttpUtils.getLocationFromHeader(response, true);// http://cq.189.cn/account/index.htm?ticket=ST-22114-JsL0DmlXa4McUodZax61
            get = HttpUtils.get(url);
            response = client.execute(get);
            response.close();
            url = "http://cq.189.cn/account/index.htm";
            responseString = HttpUtils.executeGetWithResult(client, url);
            logger.debug(responseString);
            // 马德 dwr
            url = "http://cq.189.cn/account/dwr/engine.js";
            this.scriptSessionId = loadScriptSessionId(url);
            logger.debug(this.scriptSessionId);
            this.jsessionid_app3 = HttpUtils.getFirstCookie(cookieStore, "JSESSIONID_APP3");
            logger.debug(this.jsessionid_app3);
            url = "http://cq.189.cn/account/dwr/call/plaincall/userInfoQueryDwr.getCustInfoNew.dwr";
            params = new HashMap<>();
            params.put("callCount", "1");
            params.put("page", "/account/userInfo.htm");
            params.put("httpSessionId", this.jsessionid_app3);
            params.put("scriptSessionId", getScriptSessionId());
            params.put("c0-scriptName", "userInfoQueryDwr");
            params.put("c0-methodName", "getCustInfoNew");
            params.put("c0-id", "0");
            params.put("batchId", batchId++);
            post = HttpUtils.post(url, params);
            response = client.execute(post);
            responseString = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            responseString = HttpUtils.unicodeToString(responseString);
            FileUtils.writeStringToFile(createTempFile(BILL_TYPE_PERSONALINFO), responseString, HttpUtils.UTF_8);
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
            // String url =
            // "http://www.189.cn/dqmh/frontLink.do?method=linkTo&shopId=10004&toStUrl=http://cq.189.cn/users/getTicket.htm?sendredirect=http://cq.189.cn/bill/bill.htm?id=4";
            // HttpGet get = HttpUtils.get(url);
            // CloseableHttpResponse response = client.execute(get);
            // url = HttpUtils.getLocationFromHeader(response, true);//
            // https://uam.ct10000.com/ct10000uam-gate/SSOFromUAM?ReturnURL=687474703A2F2F63712E3138392E636E2F75736572732F6765745469636B65742E68746D3F73656E6472656469726563743D687474703A2F2F63712E3138392E636E2F62696C6C2F62696C6C2E68746D3F69643D34&ProvinceId=04
            // logger.debug(url);
            // get = HttpUtils.get(url);
            // response = clientWithSSL.execute(get);
            // url = HttpUtils.getLocationFromHeader(response, true);//
            // https://uam.ct10000.com/ct10000uam/login?service=https%3A%2F%2Fuam.ct10000.com%3A443%2Fct10000uam-gate%2FSSOFromUAM%3FReturnURL%3D687474703A2F2F63712E3138392E636E2F75736572732F6765745469636B65742E68746D3F73656E6472656469726563743D687474703A2F2F63712E3138392E636E2F62696C6C2F62696C6C2E68746D3F69643D34%26ProvinceId%3D04&serviceId=001&ProvinceId=04
            // get = HttpUtils.get(url);
            // response = clientWithSSL.execute(get);
            // url = HttpUtils.getLocationFromHeader(response, true);//
            // https://uam.ct10000.com/ct10000uam-gate/?SSORequestXMLRETURN=https://uam.ct10000.com:443/ct10000uam-gate/SSOFromUAM?ReturnURL=687474703A2F2F63712E3138392E636E2F75736572732F6765745469636B65742E68746D3F73656E6472656469726563743D687474703A2F2F63712E3138392E636E2F62696C6C2F62696C6C2E68746D3F69643D34&ProvinceId=04&ProvinceUrl=687474703A2F2F3232322E3137372E342E3130312F55415765622F736572766C65742F53534F46726F6D435455414D&UATicket=35001ST--1153672-OfZLL6jUYOvd9YJQO4Fa-ct10000uam
            // String uaTicket = url.split("UATicket=")[1];
            // logger.debug(uaTicket);
            // get = HttpUtils.get(url);
            // HttpUtils.executeGet(clientWithSSL, url);
            //
            // url = "http://222.177.4.101/UAWeb/servlet/SSOFromCTUAM?UATicket="
            // + uaTicket;
            //
            // get = HttpUtils.get(url);
            // response = clientWithSSL.execute(get);
            // url = HttpUtils.getLocationFromHeader(response, true);//
            // http://cq.189.cn/users/getTicket.htm?sendredirect=http%3A%2F%2Fcq.189.cn%2Fbill%2Fbill.htm%3Fid%3D4&UATicket=047617620
            //
            // get = HttpUtils.get(url);
            // response = client.execute(get);
            // String responseString =
            // EntityUtils.toString(response.getEntity());
            // response.close();
            //
            // HttpPost post = HttpUtils.buildPostFromHtml(responseString,
            // "#frm");//
            // http://cq.189.cn/sso/login?get-lt=true&method=direct&key=3133333638303338373538235F23313131&enc=386f06e57b89fe60eb281203bce885ce&service=http%3A%2F%2Fcq.189.cn%2Fbill%2Fbill.htm%3Fid%3D4
            // response = client.execute(post);
            // url = HttpUtils.getLocationFromHeader(response, true);//
            // http://cq.189.cn/sso/login?lt=_cA1AAB70D-2970-006A-D82E-551EA58A7708_k731346C9-D0B8-07E8-B3CA-B088B4047781&_eventId=submit&method=direct&key=3133333638303338373538235F23313131&enc=386f06e57b89fe60eb281203bce885ce&service=http://cq.189.cn/bill/bill.htm?id=4
            // get = HttpUtils.get(url);
            // response = client.execute(get);
            // url = HttpUtils.getLocationFromHeader(response, true);//
            // http://cq.189.cn/bill/bill.htm?id=4&ticket=ST-24910-m4Xnh6DP7ZFsfcpOT5SM
            //
            // get = HttpUtils.get(url);
            // response = client.execute(get);
            // url = HttpUtils.getLocationFromHeader(response, true);//
            // http://cq.189.cn/bill/bill.htm?id=4

            String url = "http://cq.189.cn/bill/bill.htm?id=4";
            logger.debug(HttpUtils.executeGetWithResult(client, url));

            url = "http://cq.189.cn/bill/dwr/engine.js";
            this.scriptSessionId = loadScriptSessionId(url);
            this.jsessionid_app4 = HttpUtils.getFirstCookie(cookieStore, "JSESSIONID_APP4");
            url = "http://cq.189.cn/bill/randImage";
            HttpUtils.executeGet(client, url);

            url = "http://cq.189.cn/cms/menu.htm?mod=1002&callback=jsonp1419243496297";
            HttpUtils.executeGet(client, url);
            url = "http://cq.189.cn/bill/dwr/call/plaincall/billDwr.getListTypeMonth.dwr";
            Map<String, Object> params = new HashMap<>();
            params = new HashMap<>();
            params.put("callCount", "1");
            params.put("page", "/bill/bill.htm?id=4");
            params.put("httpSessionId", this.jsessionid_app4);
            params.put("scriptSessionId", getScriptSessionId());
            params.put("c0-scriptName", "billDwr");
            params.put("c0-methodName", "getListTypeMonth");
            params.put("c0-id", "0");
            params.put("c0-param0", "string:0");
            params.put("batchId", batchId++);
            logger.debug(HttpUtils.executePostWithResult(client, url, params));

            url = "http://cq.189.cn/bill/dwr/call/plaincall/billDwr.sendSM.dwr";
            params = new HashMap<>();
            params.put("callCount", "1");
            params.put("page", "/bill/bill.htm?id=4");
            params.put("httpSessionId", this.jsessionid_app4);
            params.put("scriptSessionId", getScriptSessionId());
            params.put("c0-scriptName", "billDwr");
            params.put("c0-methodName", "sendSM");
            params.put("c0-id", "0");
            params.put("batchId", batchId++);
            HttpPost post = HttpUtils.post(url, params);
            post.addHeader("Referer", "http://cq.189.cn/bill/bill.htm?id=4");
            post.addHeader("Content-Type", "text/plain; charset=UTF-8");
            post.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            post.addHeader("Accept-Language", "en-US,en;q=0.5");
            post.addHeader("Pragma", "no-cache");
            post.addHeader("Cache-Control", "no-cache");
            CloseableHttpResponse response = client.execute(post);
            String rs = EntityUtils.toString(response.getEntity());
            response.close();
            logger.debug(rs);
            // return (rs.indexOf("随机验证码已经发送到") > -1);
            return true;
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
            String url = "http://cq.189.cn/bill/dwr/call/plaincall/billDwr.validateListSMSRandomCode.dwr";
            Map<String, Object> params = new HashMap<>();
            params.put("callCount", "1");
            params.put("page", "/bill/bill.htm?id=4");
            params.put("httpSessionId", this.jsessionid_app4);
            params.put("scriptSessionId", getScriptSessionId());
            params.put("c0-scriptName", "billDwr");
            params.put("c0-methodName", "validateListSMSRandomCode");
            params.put("c0-id", "0");
            params.put("c0-param0", "string:" + randomCode);
            params.put("batchId", batchId++);
            HttpPost post = HttpUtils.post(url, params);
            post.addHeader("Content-Type", "text/plain; charset=UTF-8");
            post.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            post.addHeader("Accept-Language", "en-US,en;q=0.5");
            post.addHeader("Pragma", "no-cache");
            post.addHeader("Cache-Control", "no-cache");
            CloseableHttpResponse response = client.execute(post);
            String rs = EntityUtils.toString(response.getEntity());
            response.close();
            logger.debug(rs);

            url = "http://cq.189.cn/bill/dwr/call/plaincall/billDwr.getListTypeMonth.dwr";
            params = new HashMap<>();
            params.put("callCount", "1");
            params.put("page", "/bill/bill.htm?id=4");
            params.put("httpSessionId", this.jsessionid_app4);
            params.put("scriptSessionId", getScriptSessionId());
            params.put("c0-scriptName", "billDwr");
            params.put("c0-methodName", "getListTypeMonth");
            params.put("c0-id", "0");
            params.put("c0-param0", "string:0");
            params.put("batchId", batchId++);
            post = HttpUtils.post(url, params);
            post.addHeader("Content-Type", "text/plain; charset=UTF-8");
            post.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            post.addHeader("Accept-Language", "en-US,en;q=0.5");
            post.addHeader("Pragma", "no-cache");
            post.addHeader("Cache-Control", "no-cache");
            response = client.execute(post);
            rs = EntityUtils.toString(response.getEntity());
            response.close();
            logger.debug(rs);
            return true;
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
            // super.submitBillTasks();
            this.gsm();
            this.sms();
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
        commonFee(month, "string:300001", BILL_TYPE_GSM, "语音详单");
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
        commonFee(month, "string:300002", BILL_TYPE_SMS, "短信详单");
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
        try {
            String url = "http://cq.189.cn/bill/dwr/call/plaincall/billDwr.selectListType.dwr";

            Map<String, Object> params = new HashMap<>();
            params.put("callCount", "1");
            params.put("page", "/bill/bill.htm?id=4");
            params.put("httpSessionId", this.jsessionid_app4);
            params.put("scriptSessionId", getScriptSessionId());
            params.put("c0-scriptName", "billDwr");
            params.put("c0-methodName", "selectListType");
            params.put("c0-id", "0");
            params.put("c0-param0", "string:" + "0");
            params.put("c0-param1", "string:" + DateUtils.formatDate(month, PATTERN_10086));
            params.put("c0-param2", "string:" + DateUtils.getFirstDayOfMonth(month));
            params.put("c0-param3", "string:" + DateUtils.getLastDayOfMonth(month));
            params.put("c0-param4", type);
            params.put("c0-param5", "string:" + "01");
            params.put("batchId", batchId++);
            HttpPost post = HttpUtils.post(url, params);
            post.addHeader("Content-Type", "text/plain; charset=UTF-8");
            post.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            post.addHeader("Accept-Language", "en-US,en;q=0.5");
            post.addHeader("Pragma", "no-cache");
            post.addHeader("Cache-Control", "no-cache");
            CloseableHttpResponse response = client.execute(post);
            String rs = EntityUtils.toString(response.getEntity());
            response.close();
            logger.debug(rs);
            // url =
            // "http://cq.189.cn/bill/dwr/call/plaincall/billDwr.bsnPageHead.dwr";
            url = "http://cq.189.cn/bill/dwr/call/plaincall/billDwr.bsnPage.dwr";
            params.put("c0-methodName", "bsnPage");
            params.put("c0-param0", "Object_Object:{}");
            params.put("c0-param1", "Object_Object:{}");
            params.put("c0-param2", "Object_Object:{}");
            params.put("c0-param3", "number:1");
            params.put("c0-param4", "number:100");
            params.remove("c0-param5");
            params.put("batchId", batchId++);
            post = HttpUtils.post(url, params);
            post.addHeader("Content-Type", "text/plain; charset=UTF-8");
            post.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            post.addHeader("Accept-Language", "en-US,en;q=0.5");
            post.addHeader("Pragma", "no-cache");
            post.addHeader("Cache-Control", "no-cache");
            response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            responseString = HttpUtils.unicodeToString(responseString);
            FileUtils.write(createTempFile(billType), responseString, HttpUtils.UTF_8);
            response.close();
        } catch (Exception e) {
            logger.error("获取[" + billType + "]失败", e);
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
            if (clientUamCqCt10000 != null) {
                clientUamCqCt10000.close();
            }
            if (clientCqCt10000 != null) {
                clientCqCt10000.close();
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
        return "cq";
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

    private String loadScriptSessionId(String url) {
        try {
            // 马德 dwr
            String responseString = HttpUtils.executeGetWithResult(client, url);
            logger.debug(responseString);
            String regEx = "dwr\\.engine\\._origScriptSessionId = \"(\\w+)\"";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(responseString);
            while (m.find()) {
                return m.group(1);
            }
            logger.debug(this.scriptSessionId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private String getScriptSessionId() {
        return this.scriptSessionId + Math.floor(Math.random() * 1000);
    }

    public static void main(String[] args) throws IOException {
        // String s =
        // "throw 'allowScriptTagRemoting is false.';\r#DWR-INSERT\r#DWR-REPLYdwr.engine._remoteHandleCallback('0','0',{accNbr:null,addressee:null,areaCode:null,birthday:null,cardType:null,contactCust:null,contactPhone:\"1858075**62\",custAddr:\"\u91CD\u5E86\u5E02\u9149\u9633\u53BF\u949F\u591A\u9547\u4E1C\u98CE\u8857***\u53F7\",custCode:null,custId:null,deliverAddr:\"\",email:null,emailAddr:\"\",emailCode:null,getPassAsk:null,getPassQuestion:null,idCardNumber:\"500242********0017\",idCardType:\"\u672C\u5730\u8EAB\u4EFD\u8BC1\",individualDesc:null,installAddr:null,isAgreed:null,isNewUser:null,loginType:null,note:\"000\",notePhone:null,postAddr:null,postCode:null,prodType:null,receiveNum:\"\",receiver:\"\",receiverPC:\"\",receiverPho:\"\",relaMobile:null,relaPhsPhone:null,selfHoodImage:null,sex:null,showNumber:\"500242********0017\",transAreaCode:null,userName:\"\u9648\u6B46\u4E88\",userNikeName:null,userSex:null});";
        // s = new String(s.getBytes(HttpUtils.UTF_8), HttpUtils.UTF_8);
        // FileUtils.write(new File("D:/ddd.txt"), s, HttpUtils.UTF_8);
        // System.out.println(s);
        System.out.println(Math.floor(Math.random() * 1000));
    }
}
