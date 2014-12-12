package com.puhui.crawler.mobile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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
import org.dom4j.DocumentHelper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amos.tool.PropertiesUtil;
import com.puhui.crawler.util.DateUtils;
import com.puhui.crawler.util.HttpUtils;

/**
 * 北京電信
 * 
 * @author zhuyuhang
 */
public class CT_BJ_MobileFetcher extends MobileFetcher {
    private Logger logger = Logger.getLogger(CT_BJ_MobileFetcher.class);
    private String ssoSessionID;
    private CloseableHttpClient client;
    private static final String PATTERN_10086 = "yyyy.MM";
    private CookieStore cookieStore = new BasicCookieStore();

    public CT_BJ_MobileFetcher() {
        this.client = HttpUtils.getHttpClient(true, cookieStore);
    }

    @Override
    public File loadCaptchaCode() {
        try {
            String url = "http://www.189.cn/dqmh/createCheckCode.do?method=checkcode&date="
                    + System.currentTimeMillis();
            HttpGet get = HttpUtils.get(url);
            CloseableHttpResponse response = client.execute(get);
            File codeFile = new File(PropertiesUtil.getProps("mobile.captcha.dir"), System.currentTimeMillis() + ".jpg");
            FileUtils.copyInputStreamToFile(response.getEntity().getContent(), codeFile);
            response.close();
            logger.debug("bj.cdma获取验证码");
            return codeFile;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean checkCaptchaCode() {
        try {
            String url = "http://www.189.cn/dqmh/chongzhi.do?method=queryPhoneNumberAccount";
            Map<String, Object> params = new HashMap<>();
            params.put("phoneNumber", getPhone());
            String result = HttpUtils.executePostWithResult(client, url, params);
            logger.debug(result);

            // 验证验证码
            url = "http://www.189.cn/dqmh/createCheckCode.do?method=checkCheckCode";
            params = new HashMap<>();
            params.put("number", getPhone());
            params.put("code", this.getCaptchaCode());
            params.put("date", System.currentTimeMillis());

            HttpPost post = HttpUtils.post(url, params);
            CloseableHttpResponse response = client.execute(post);
            response.close();
            return true;
        } catch (Exception e) {
            logger.error("cdma验证附加码错误", e);
        }
        return false;
    }

    @Override
    public boolean login(String phone, String password, String rnum) {
        super.login(phone, password, rnum);
        try {
            return this.prepare() && this.checkCaptchaCode() && this.login();
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

    private String makeParam(String xml) {
        try {
            org.dom4j.Document document = DocumentHelper.parseText(xml);
            org.dom4j.Element root = document.getRootElement();
            org.dom4j.Element sessionBody = root.element("SessionBody");
            org.dom4j.Element cT10000SSOAuthReq = sessionBody.element("CT10000SSOAuthReq");
            org.dom4j.Element redirectURL = cT10000SSOAuthReq.element("RedirectURL");
            redirectURL.setText(URLEncoder.encode(
                    "http://bj.189.cn/service/account/customerHome.action?rand=" + System.currentTimeMillis(),
                    HttpUtils.UTF_8));
            System.out.println(redirectURL.getText());
            return document.asXML().replace("\n", "").replaceAll("<", "%3C").replaceAll("\"", "%22")
                    .replaceAll(">", "%3E").replace(" ", "%20");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xml;
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
            cookieStore.addCookie(new BasicClientCookie("cityCode", "bj"));
            String url = "http://www.189.cn/dqmh/UnifiedLogin.do?method=unifiedTicketCallBack&backUrl=http://bj.189.cn/service/account/customerHome.action";
            HttpGet get = HttpUtils.get(url);
            client.execute(get).close();
            logger.debug("cdma预备");
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
            String url = "http://www.189.cn/dqmh/login/beijinglogin.jsp";
            String html = HttpUtils.executeGetWithResult(client, url);
            Map<String, Object> params = HttpUtils.buildParamsFromHtml(html, "#c2000004");
            params.put("backurl", "http://bj.189.cn/service/account/customerHome.action");
            params.put("cityCode", "10001");
            params.put("phoneNumber", getPhone());
            params.put("phonePwdType", "01");
            params.put("phonePassWord", getPassword());
            params.put("verificationCode", getCaptchaCode());

            url = "http://www.189.cn/dqmh/Uam.do?method=uamUnifiedLogin&uamType=phone&loginType=201";
            HttpPost post = HttpUtils.post(url, params);
            CloseableHttpResponse response = client.execute(post);
            // 获取重定向url
            // http://uam.bj.ct10000.com/LoginIn?SSORequestXML=%3CCAPRoot%3E%3CSessionHeader%3E%3CServiceCode%3ECAP01005%3C/ServiceCode%3E%3CVersion%3E1234567890123456%3C/Version%3E%3CActionCode%3E0%3C/ActionCode%3E%3CTransactionID%3E01001201412021234567890%3C/TransactionID%3E%3CSrcSysID%3E35111%3C/SrcSysID%3E%3CDstSysID%3E01%3C/DstSysID%3E%3CReqTime%3E20141202141742%3C/ReqTime%3E%3CDigitalSign%20/%3E%3C/SessionHeader%3E%3CSessionBody%3E%3CCT10000SSOAuthReq%3E%3CRedirectURL%3Ehttp%3A%2F%2Fwww.189.cn%2Fdqmh%2FUnifiedLogin.do%3Fmethod%3DunifiedTicketCallBack%26amp%3BloginType%3D201%26amp%3BbackUrl%3Dhttp%3A%2F%2Fbj.189.cn%2Fservice%2Faccount%2FcustomerHome.action%3C/RedirectURL%3E%3CAuthInfo%3EjSGGcLweLCjlRkInFg4hE4rluI7X7kv0DFs93GjqtoVqWIWP3TaObeqmxo4wq%2B9YFn2yHBukbHRz963HLNRd9n%2BYUeIAjEgQdQcgcEuFP8FEjJVQxEnixfDjpsoy8P1faQo1sccbpEPyhi%2FD8qg81K8riOmzB5yk%3C/AuthInfo%3E%3C/CT10000SSOAuthReq%3E%3C/SessionBody%3E%3C/CAPRoot%3E
            url = HttpUtils.getHeader(response, "Location");
            response.close();
            if (url == null) {
                return false;
            }
            params = new HashMap<>();
            params.put("SSORequestXML", url.split("SSORequestXML=")[1]);
            url = "http://uam.bj.ct10000.com/LoginIn";
            // url = HttpUtils.encodeUrl(url);
            post = HttpUtils.post(url, params);
            post.addHeader("Referer", "http://www.189.cn/dqmh/login/beijinglogin.jsp");
            response = client.execute(post);
            url = HttpUtils.getHeader(response, "Location");
            html = EntityUtils.toString(response.getEntity());
            HttpGet get = HttpUtils.get(url);
            response = client.execute(get);
            response.close();

            url = "http://bj.189.cn/service/account/customerHome.action?rand=" + System.currentTimeMillis();
            get = HttpUtils.get(url);
            response = client.execute(get);
            url = HttpUtils.getHeader(response, "Location");
            response.close();
            if (url == null) {
                return false;
            }

            String xml = url.split("SSORequestXML=")[1];
            url = "http://uam.bj.ct10000.com/LoginAuth?SSORequestXML=" + makeParam(xml);
            get = HttpUtils.get(url);
            get.addHeader(
                    "Referer",
                    "http://www.189.cn/dqmh/UnifiedLogin.do?method=unifiedTicketCallBack&backUrl=http://bj.189.cn/service/account/customerHome.action");
            get.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            get.addHeader("Accept-Language", "en-US,en;q=0.5");
            response = client.execute(get);
            // http://bj.189.cn/service/account/customerHome.action?rand=1417501063540&UATicket=1103DB150F53890903B381E1BD6CF2110906C208B4A038F0A8BDABFF735D7E9F
            url = HttpUtils.getHeader(response, "Location");
            html = EntityUtils.toString(response.getEntity());
            response.close();

            html = HttpUtils.executeGetWithResult(client, url);

            url = "http://bj.189.cn/service/account/customerName.action";
            params = new HashMap<>();
            params.put("time", new Date());
            post = HttpUtils.post(url, params);
            String r = HttpUtils.executePostWithResult(client, url, null);
            logger.debug(r);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
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
        return "bj";
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
