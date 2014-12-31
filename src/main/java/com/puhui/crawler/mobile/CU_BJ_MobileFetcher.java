package com.puhui.crawler.mobile;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puhui.crawler.util.DateUtils;
import com.puhui.crawler.util.HttpUtils;

/**
 * 中国联通
 * 
 * @author zhuyuhang
 */
public class CU_BJ_MobileFetcher extends MobileFetcher {
    private static Logger logger = Logger.getLogger(CU_BJ_MobileFetcher.class);
    private CookieStore cookieStore = new BasicCookieStore();
    private CloseableHttpClient client;
    private String packageName = null;
    private static final String PATTERN = "yyyyMM";

    public CU_BJ_MobileFetcher() {
    }

    /**
     * 登录
     * 
     * @author zhuyuhang
     * @return 返回登录后的session会话
     */
    @Override
    public boolean login(String phone, String password, String rnum) {
        super.login(phone, password, rnum);
        CloseableHttpClient client = HttpUtils.getHttpClient(true, cookieStore);
        String url = "https://uac.10010.com/portal/Service/MallLogin?1=1";
        Map<String, Object> params = new HashMap<>();
        params.put("userName", phone);
        params.put("password", password);
        params.put("pwdType", "01");
        params.put("productType", "01");
        params.put("redirectType", "01");
        params.put("_", System.currentTimeMillis());
        url = url + HttpUtils.buildParamString(params);
        HttpGet get = HttpUtils.get(url);
        try {
            CloseableHttpResponse response = client.execute(get);
            String result = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            JSONObject json = JSON.parseObject(result);
            String resultCode = json.getString("resultCode");
            if ("0000".equals(resultCode)) {
                logger.info("登录成功[" + phone + "," + password + "]");
                url = "http://iservice.10010.com/e3/static/common/info?_=" + System.currentTimeMillis();
                HttpUtils.executePost(client, url);
                url = "http://iservice.10010.com/e3/static/header";
                HttpUtils.executePost(client, url);
                url = "http://iservice.10010.com/e3/static/check/checklogin/?_" + System.currentTimeMillis();
                HttpPost post = HttpUtils.post(url);
                response = client.execute(post);
                result = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
                response.close();
                json = JSON.parseObject(result);
                if (!json.getBooleanValue("isLogin")) {
                    return false;
                }
                FileUtils
                        .write(createTempFile(BILL_TYPE_PERSONALINFO), result, Charset.forName(HttpUtils.UTF_8), false);
                JSONObject userInfo = json.getJSONObject("userInfo");
                String packageName = userInfo.getString("packageName");// 套餐名称
                if (packageName != null) {
                    packageName = packageName.toLowerCase();
                    if (packageName.contains("4g")) {
                        this.packageName = "4g";
                    } else if (packageName.contains("3g")) {
                        this.packageName = "3g";
                    }
                }
                this.client = client;
                return true;
            } else {
                logger.info("登录失败[" + phone + "," + password + ", " + result + "]");
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean loadBills() {
        try {
            this.hisBill();
            this.gsm();
            this.sms();
            this.gprs();
            this.addvalue();
            this.address();
            this.personalInfo();
            this.accountBalance();
            loadBillsSuccessfully = true;
        } finally {
            this.close();
        }
        return true;
    }

    /**
     * 信息总览
     * 
     * @author zhuyuhang
     * @param client
     */
    @Override
    protected void personalInfo() {
    }

    private boolean is3g() {
        return "3g".equals(this.packageName);
    }

    /**
     * 历史账单
     * 
     * @author zhuyuhang
     * @param client
     */
    @Override
    protected void hisBill() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            date = DateUtils.addMonths(date, -1);
            if (is3g()) {
                hisBill3g(date);
            } else {
                hisBill(date);
            }
        }
    }

    /**
     * 历史账单
     * 
     * @author zhuyuhang
     * @param client
     */
    private void hisBill(Date date) {
        this.checkLogin();
        logger.info("获取历史账单(" + DateUtils.formatDate(date) + ")");
        String url = "http://iservice.10010.com/e3/static/query/queryHistoryBill?_=" + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put("quertype", "0001");
        params.put("querycode", "0001");
        params.put("billdate", DateUtils.formatDate(date, "yyyyMM"));
        httppost.setEntity(HttpUtils.buildParams(params));
        try {
            CloseableHttpResponse response = this.client.execute(httppost);
            String resp = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            FileUtils.write(createTempFile(BILL_TYPE_HISBILL), resp, Charset.forName(HttpUtils.UTF_8), true);
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 联通3g历史账单
     * 
     * @author zhuyuhang
     * @param date
     */
    private void hisBill3g(Date date) {
        try {
            String ms = DateUtils.formatDate(date, PATTERN);
            String url = "http://iservice.10010.com/ehallService/static/historyBiil/execute/YH102010002/QUERY_YH102010002.processData/QueryYH102010002_Data/"
                    + ms + "/undefined?menuid=000100020001&_=" + System.currentTimeMillis();
            String resp = HttpUtils.executePostWithResult(client, url, null);
            FileUtils.write(createTempFile(BILL_TYPE_HISBILL + "_" + "3g"), resp, Charset.forName(HttpUtils.UTF_8),
                    true);
        } catch (Exception e) {
            logger.error("查询3g历史账单失败", e);
        }
    }

    /**
     * 通话详单
     * 
     * @author zhuyuhang
     * @param client
     */
    @Override
    protected void gsm() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            if (is3g()) {
                gsm3g(date);
            } else {
                gsm(date);
            }
            date = DateUtils.addMonths(date, -1);
        }
    }

    /**
     * date所在月通话详单
     * 
     * @author zhuyuhang
     * @param client
     * @param date
     */
    private int gsmRetryTimes = 0;

    private void gsm(Date date) {
        if (gsmRetryTimes > 5) {
            return;
        }
        gsmRetryTimes++;
        // gsm(date, 1, 1);
        logger.info("获取通话详单(" + DateUtils.formatDate(date, "yyyyMM"));
        String url = "http://iservice.10010.com/e3/static/query/callDetail?_=" + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", PAGE_SIZE);
        params.put("beginDate", DateUtils.getFirstDayOfMonth(date));
        params.put("endDate", DateUtils.getLastDayOfMonth(date));
        httppost.setEntity(HttpUtils.buildParams(params));
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String r = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            JSONObject json = JSON.parseObject(r);
            boolean isSuccess = json.getBooleanValue("isSuccess");
            if (isSuccess) {
                int totalRecord = json.getIntValue("totalRecord");
                if (totalRecord > 0) {
                    JSONObject pageMap = json.getJSONObject("pageMap");
                    if (pageMap != null) {
                        JSONArray result = pageMap.getJSONArray(("result"));
                        FileUtils.write(createTempFile(BILL_TYPE_GSM), result.toString(),
                                Charset.forName(HttpUtils.UTF_8), true);
                    }
                }
                return;
            } else {
                gsm(date);
            }
        } catch (Exception e) {
            logger.error("获取通话详单失败", e);
        }
    }

    /**
     * 通话详单
     * 
     * @author zhuyuhang
     * @param date
     * @param pageNo
     * @return
     */
    private void gsm(Date date, int pageNo, int _totalPages) {
        this.checkLogin();
        if (pageNo > _totalPages) {
            return;
        }
        logger.info("获取通话详单(" + DateUtils.formatDate(date, "yyyyMM") + ", " + pageNo + ", " + _totalPages + ")");
        String url = "http://iservice.10010.com/e3/static/query/callDetail?_=" + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", pageNo);
        params.put("pageSize", PAGE_SIZE);
        params.put("beginDate", DateUtils.getFirstDayOfMonth(date));
        params.put("endDate", DateUtils.getLastDayOfMonth(date));
        httppost.setEntity(HttpUtils.buildParams(params));
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String r = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            JSONObject json = JSON.parseObject(r);
            boolean isSuccess = json.getBooleanValue("isSuccess");
            if (isSuccess) {
                int totalRecord = json.getIntValue("totalRecord");
                if (totalRecord > 0) {
                    JSONObject pageMap = json.getJSONObject("pageMap");
                    if (pageMap != null) {
                        int totalPages = pageMap.getIntValue(("totalPages"));
                        JSONArray result = pageMap.getJSONArray(("result"));
                        FileUtils.write(createTempFile(BILL_TYPE_GSM), result.toString(),
                                Charset.forName(HttpUtils.UTF_8), true);
                        gsm(date, ++pageNo, totalPages);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 3g通话详单
     * 
     * @author zhuyuhang
     * @param month
     */
    private void gsm3g(Date month) {
        try {
            String url = "http://iservice.10010.com/ehallService/static/queryMonth/checkmapExtraParam/0001";
            String ms = DateUtils.formatDate(month, PATTERN);
            HttpUtils.executePost(client, url);
            url = "http://iservice.10010.com/ehallService/static/queryMonth/execute2/YHgetMonths/QUERY_paramSession.processData/QUERY_paramSession_Data/000100030001/"
                    + ms + "/undefined/undefined/undefined?menuid=000100030001&_=" + System.currentTimeMillis();
            HttpUtils.executePost(client, url);
            url = "http://iservice.10010.com/ehallService/view/page/mySimpleCallDetail.html";
            Map<String, Object> params = new HashMap<>();
            params.put("yuefen", ms);
            params.put("startDate", DateUtils.getFirstDayOfMonth(month));
            params.put("endDate", DateUtils.getLastDayOfMonth(month));
            HttpUtils.executePost(client, url, params);
            url = "http://iservice.10010.com/ehallService/static/callDetail/execute/YH102010006/Query_YH102010006.processData/QueryYH102010006_Data/true/1/"
                    + PAGE_SIZE + "?_=" + System.currentTimeMillis();
            HttpPost request = HttpUtils.post(url);
            CloseableHttpResponse response = client.execute(request);
            String resp = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            FileUtils.write(createTempFile(BILL_TYPE_GSM + "_" + "3g"), resp, Charset.forName(HttpUtils.UTF_8), true);
        } catch (Exception e) {
            logger.error("查询3g通话详单失败", e);
        }
    }

    /**
     * 短信详单
     * 
     * @author zhuyuhang
     */
    protected void sms() {
        Date date = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            if (is3g()) {
                sms3g(date);
            } else {
                sms(date);
            }
            date = DateUtils.addMonths(date, -1);
        }
    }

    /**
     * date所在月短信详单
     * 
     * @author zhuyuhang
     * @param date
     */
    private int smsRetryTimes = 0;

    private void sms(Date date) {
        // sms(date, 1, 1);
        if (smsRetryTimes > 5) {// 马德 突然发现有时候丫就不给
            return;
        }
        logger.info("获取短信详单" + DateUtils.formatDate(date));
        String url = "http://iservice.10010.com/e3/static/query/sms?_=" + System.currentTimeMillis();
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", PAGE_SIZE);
        params.put("begindate", DateUtils.getFirstDayOfMonth(date, "yyyyMMdd"));
        params.put("enddate", DateUtils.getLastDayOfMonth(date, "yyyyMMdd"));
        HttpPost httppost = HttpUtils.post(url, params);
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String r = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            JSONObject json = JSON.parseObject(r);
            boolean isSuccess = json.getBooleanValue("isSuccess");
            if (isSuccess) {
                int totalRecord = json.getIntValue("smsCount");
                if (totalRecord > 0) {
                    JSONObject pageMap = json.getJSONObject("pageMap");
                    if (pageMap != null) {
                        JSONArray result = pageMap.getJSONArray(("result"));
                        FileUtils.write(createTempFile(BILL_TYPE_SMS), result.toString(),
                                Charset.forName(HttpUtils.UTF_8), true);
                    }
                }
                return;
            } else {
                sms(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 短信详单
     * 
     * @author zhuyuhang
     */
    private void sms(Date date, int pageNo, int _totalPages) {
        this.checkLogin();
        if (pageNo > _totalPages) {
            return;
        }
        logger.info("获取短信详单(" + DateUtils.formatDate(date) + ", " + pageNo + ", " + _totalPages + ")");
        String url = "http://iservice.10010.com/e3/static/query/sms?_=" + System.currentTimeMillis();
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", pageNo);
        params.put("pageSize", PAGE_SIZE);
        params.put("begindate", DateUtils.getFirstDayOfMonth(date, "yyyyMMdd"));
        params.put("enddate", DateUtils.getLastDayOfMonth(date, "yyyyMMdd"));
        HttpPost httppost = HttpUtils.post(url, params);
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String r = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            JSONObject json = JSON.parseObject(r);
            boolean isSuccess = json.getBooleanValue("isSuccess");
            if (isSuccess) {
                int totalRecord = json.getIntValue("smsCount");
                if (totalRecord > 0) {
                    JSONObject pageMap = json.getJSONObject("pageMap");
                    if (pageMap != null) {
                        int totalPages = pageMap.getIntValue(("totalPages"));
                        JSONArray result = pageMap.getJSONArray(("result"));
                        FileUtils.write(createTempFile(BILL_TYPE_SMS), result.toString(),
                                Charset.forName(HttpUtils.UTF_8), true);
                        sms(date, ++pageNo, totalPages);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 3g短信详单
     * 
     * @author zhuyuhang
     * @param month
     */
    private void sms3g(Date month) {
        try {
            String url = "http://iservice.10010.com/ehallService/static/queryMonth/checkmapExtraParam/0002";
            String ms = DateUtils.formatDate(month, PATTERN);
            HttpUtils.executePost(client, url);
            url = "http://iservice.10010.com/ehallService/static/queryMonth/execute2/YHgetMonths/QUERY_paramSession.processData/QUERY_paramSession_Data/000100030002/"
                    + ms + "/undefined/undefined/undefined?menuid=000100030002&_=" + System.currentTimeMillis();
            HttpUtils.executePost(client, url);
            url = "http://iservice.10010.com/ehallService/view/page/mySimpleCallDetail.html";
            Map<String, Object> params = new HashMap<>();
            params.put("yuefen", ms);
            params.put("startDate", DateUtils.getFirstDayOfMonth(month));
            params.put("endDate", DateUtils.getLastDayOfMonth(month));
            HttpUtils.executePost(client, url, params);
            url = "http://iservice.10010.com/ehallService/static/SMSDetail/execute/YH102010007/QUERY_YH102010007.processData/QueryYH102010007_Data/false/1/"
                    + PAGE_SIZE + "?_=" + System.currentTimeMillis();
            HttpPost request = HttpUtils.post(url);
            CloseableHttpResponse response = client.execute(request);
            String resp = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            FileUtils.write(createTempFile(BILL_TYPE_SMS + "_" + "3g"), resp, Charset.forName(HttpUtils.UTF_8), true);
        } catch (Exception e) {
            logger.error("查询3g通话详单失败", e);
        }
    }

    /**
     * 增值业务
     * 
     * @author zhuyuhang
     * @param client
     */
    @Override
    protected void addvalue() {
        Date now = new Date();
        for (int i = 0; i < MOBILE_BILLS_MONTH_COUNT; i++) {
            now = DateUtils.addMonths(now, -i);
            if (is3g()) {
                this.addvalue3g(now);
            } else {
                this.addvalue(now);
            }
        }
    }

    /**
     * date所在月增值业务
     * 
     * @author zhuyuhang
     * @param date
     */
    private void addvalue(Date date) {
        addvalue(date, 1, 1);
    }

    /**
     * 增值业务
     * 
     * @author zhuyuhang
     */
    private void addvalue(Date date, int pageNo, int _totalPages) {
        this.checkLogin();
        if (pageNo > _totalPages) {
            return;
        }
        logger.info("获取增值业务(" + DateUtils.formatDate(date) + ", " + pageNo + ", " + _totalPages + ")");
        String url = "http://iservice.10010.com/e3/static/query/callValueAdded?_=" + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", pageNo);
        params.put("pageSize", PAGE_SIZE);
        params.put("beginDate", DateUtils.getFirstDayOfMonth(date));
        params.put("endDate", DateUtils.getLastDayOfMonth(date));
        httppost.setEntity(HttpUtils.buildParams(params));
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String r = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            JSONObject json = JSON.parseObject(r);
            boolean isSuccess = json.getBooleanValue("isSuccess");
            if (isSuccess) {
                int totalRecord = json.getIntValue("totalRecord");
                if (totalRecord > 0) {
                    JSONObject pageMap = json.getJSONObject(("pageMap"));
                    if (pageMap != null) {
                        int totalPages = pageMap.getIntValue(("totalPages"));
                        JSONArray result = pageMap.getJSONArray(("result"));
                        FileUtils.write(createTempFile(BILL_TYPE_ADDVALUE), result.toString(),
                                Charset.forName(HttpUtils.UTF_8), true);
                        addvalue(date, ++pageNo, totalPages);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addvalue3g(Date month) {
        try {
            String url = "http://iservice.10010.com/ehallService/static/queryMonth/checkmapExtraParam/0003";
            String ms = DateUtils.formatDate(month, PATTERN);
            HttpUtils.executePost(client, url);
            url = "http://iservice.10010.com/ehallService/static/queryMonth/execute2/YHgetMonths/QUERY_paramSession.processData/QUERY_paramSession_Data/000100030003/"
                    + ms + "/undefined/undefined/undefined?menuid=000100030003&_=" + System.currentTimeMillis();
            HttpUtils.executePost(client, url);
            url = "http://iservice.10010.com/ehallService/view/page/mySimpleIncreaseDetail.html";
            Map<String, Object> params = new HashMap<>();
            params.put("yuefen", ms);
            params.put("startDate", DateUtils.getFirstDayOfMonth(month));
            params.put("endDate", DateUtils.getLastDayOfMonth(month));
            HttpUtils.executePost(client, url, params);
            url = "http://iservice.10010.com/ehallService/static/increaseDetail/execute/YH102010008/QUERY_YH102010008.processData/QueryYH102010008_Data/true/1/"
                    + PAGE_SIZE + "?_=" + System.currentTimeMillis();
            HttpPost request = HttpUtils.post(url);
            CloseableHttpResponse response = client.execute(request);
            String resp = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            FileUtils.write(createTempFile(BILL_TYPE_ADDVALUE + "_" + "3g"), resp, Charset.forName(HttpUtils.UTF_8),
                    true);
        } catch (Exception e) {
            logger.error("查询3g增值业务失败", e);
        }
    }

    /**
     * 上网流量
     * 
     * @author zhuyuhang
     * @param client
     */
    private void callFlow(Date date) {
        this.checkLogin();
        String url = "http://iservice.10010.com/e3/static/query/callFlow?_=" + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", PAGE_SIZE);
        params.put("beginDate", DateUtils.getFirstDayOfMonth(date));
        params.put("endDate", DateUtils.getLastDayOfMonth(date));
        httppost.setEntity(HttpUtils.buildParams(params));
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String resp = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            FileUtils.write(createTempFile("callFlow"), resp, Charset.forName(HttpUtils.UTF_8), true);
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 手机上网记录 貌似只有两个月的数据可查
     * 
     * @author zhuyuhang
     */
    protected void gprs() {
        Date now = new Date();
        int l = is3g() ? MOBILE_BILLS_MONTH_COUNT : 2;
        for (int i = 0; i < l; i++) {
            if (is3g()) {
                gprs3g(now);
                now = DateUtils.addMonths(now, -i);
            } else {
                now = DateUtils.addMonths(now, -i);
                gprs(now);
            }
        }
    }

    /**
     * 3g上网流量
     * 
     * @author zhuyuhang
     * @param month
     */
    private void gprs3g(Date month) {
        try {
            String url = "http://iservice.10010.com/ehallService/static/queryMonth/checkmapExtraParam/0004";
            String ms = DateUtils.formatDate(month, PATTERN);
            HttpUtils.executePost(client, url);
            url = "http://iservice.10010.com/ehallService/static/queryMonth/execute2/YHgetMonths/QUERY_paramSession.processData/QUERY_paramSession_Data/000100030004/"
                    + ms + "/undefined/undefined/undefined?menuid=000100030004&_=" + System.currentTimeMillis();
            HttpUtils.executePost(client, url);
            url = "http://iservice.10010.com/ehallService/view/page/mySimplePhoneNetFlowDetail.html";
            Map<String, Object> params = new HashMap<>();
            params.put("yuefen", ms);
            params.put("startDate", DateUtils.getFirstDayOfMonth(month));
            params.put("endDate", DateUtils.getLastDayOfMonth(month));
            HttpUtils.executePost(client, url, params);
            url = "http://iservice.10010.com/ehallService/static/phoneNetFlow/execute/YH102010014/_QUERY_YH102010014.processData/QueryYH102010014_Data/true/1/"
                    + PAGE_SIZE + "?_=" + System.currentTimeMillis();
            HttpPost request = HttpUtils.post(url);
            CloseableHttpResponse response = client.execute(request);
            String resp = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            FileUtils.write(createTempFile(BILL_TYPE_GPRS + "_3g"), resp, Charset.forName(HttpUtils.UTF_8), true);
        } catch (Exception e) {
            logger.error("查询3g上网流量失败", e);
        }
    }

    /**
     * date所在月手机上网记录
     * 
     * @author zhuyuhang
     * @param date
     */
    private void gprs(Date date) {
        gprs(date, 1, 1);
    }

    /**
     * 手机上网记录
     * 
     * @author zhuyuhang
     */
    private void gprs(Date date, int pageNo, int _totalPages) {
        this.checkLogin();
        if (pageNo > _totalPages) {
            return;
        }
        logger.info("获取手机上网记录(" + DateUtils.formatDate(date) + ", " + pageNo + ", " + _totalPages + ")");
        String url = "http://iservice.10010.com/e3/static/query/callNetPlayRecord?_=" + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", pageNo);
        params.put("pageSize", PAGE_SIZE);
        params.put("beginDate", DateUtils.getFirstDayOfMonth(date));
        params.put("endDate", DateUtils.getLastDayOfMonth(date));
        httppost.setEntity(HttpUtils.buildParams(params));
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String r = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            JSONObject json = JSON.parseObject(r);
            boolean isSuccess = json.getBooleanValue(("isSuccess"));
            if (isSuccess) {
                int totalRecord = json.getIntValue(("totalRecord"));
                if (totalRecord > 0) {
                    JSONObject pageMap = json.getJSONObject(("pageMap"));
                    if (pageMap != null) {
                        int totalPages = pageMap.getIntValue(("totalPages"));
                        JSONArray result = pageMap.getJSONArray(("result"));
                        FileUtils.write(createTempFile(BILL_TYPE_GPRS), result.toString(),
                                Charset.forName(HttpUtils.UTF_8), true);
                        gprs(date, ++pageNo, totalPages);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void accountBalance() {
        if (is3g()) {
            accountBalance3g();
            return;
        }
        try {
            logger.debug("当前余额");
            String url = "http://iservice.10010.com/e3/static/query/accountBalance/search?_="
                    + System.currentTimeMillis();
            Map<String, Object> params = new HashMap<>();
            params.put("type", "onlyAccount");
            String resp = HttpUtils.executePostWithResult(client, url, params);
            JSONObject json = JSON.parseObject(resp);
            if (!json.getBooleanValue("isError")) {
                writeToFile(createTempFile(BILL_TYPE_CURRFEE), resp);
            }
        } catch (Exception e) {
            logger.error("获取当前余额失败");
        }
    }

    @Override
    protected void currFee() {

    }

    private void accountBalance3g() {
        try {
            logger.debug("当前余额");
            String url = "http://iservice.10010.com/ehallService/static/querybalance/execute/Query_YHhead.processData/Query_YHhead_Data?_="
                    + System.currentTimeMillis();
            String resp = HttpUtils.executePostWithResult(client, url, null);
            writeToFile(createTempFile(BILL_TYPE_ACCOUNTBALANCE + "_3g"), resp);
        } catch (Exception e) {
            logger.error("获取当前余额失败");
        }
    }

    @Override
    protected void address() {
        try {
            logger.debug("收货地址");
            String url = "https://uac.10010.com/cust/postaddr/showPostAddrInfo?" + System.currentTimeMillis();
            String resp = HttpUtils.executePostWithResult(client, url, null);
            writeToFile(createTempFile(BILL_TYPE_ADDRESS + (is3g() ? "_3g" : "")), resp);
        } catch (Exception e) {
            logger.error("获取当前余额失败");
        }
    }

    private void checkLogin() {
        if (this.client == null) {
            throw new UnsupportedOperationException("还没有登录，先调用login方法");
        }
    }

    @Override
    public boolean hasCaptcha() {
        return false;
    }

    @Override
    public File loadCaptchaCode() {
        return null;
    }

    @Override
    public boolean checkCaptchaCode(String captchaCode) {
        return false;
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
    public void close() {
        super.close();
        if (this.client != null) {
            try {
                if (loadBillsSuccessfully) {
                    this.client.close();
                    this.client = null;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public String getIspSimpleName() {
        return ISP_CU;
    }

    @Override
    public String getAreaSimpleName() {
        return "bj";
    }
}
