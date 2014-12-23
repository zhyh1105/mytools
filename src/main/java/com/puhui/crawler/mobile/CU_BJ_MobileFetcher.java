package com.puhui.crawler.mobile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import com.amos.tool.PropertiesUtil;
import com.puhui.crawler.Messages;
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
    private static final int PAGE_SIZE = NumberUtils.toInt(PropertiesUtil.getProps("cu.count.per.page"), 1000000);
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
        CloseableHttpClient client = HttpUtils.getHttpClient(false, cookieStore);
        String url = Messages.getString("ChinaUnicom.login.url");
        Map<String, Object> params = new HashMap<>();
        params.put(Messages.getString("ChinaUnicom.login.param.userName"), phone);
        params.put(Messages.getString("ChinaUnicom.login.param.password"), password);
        params.put(Messages.getString("ChinaUnicom.login.param.pwdType"),
                Messages.getString("ChinaUnicom.login.param.pwdType.value"));
        params.put(Messages.getString("ChinaUnicom.login.param.productType"),
                Messages.getString("ChinaUnicom.login.param.productType.value"));
        params.put(Messages.getString("ChinaUnicom.login.param.redirectType"),
                Messages.getString("ChinaUnicom.login.param.redirectType.value"));
        params.put(Messages.getString("ChinaUnicom.login.param._"), System.currentTimeMillis());
        url = url + HttpUtils.buildParamString(params);
        HttpGet get = HttpUtils.get(url);
        try {
            CloseableHttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
                response.close();
                JSONObject json = JSON.parseObject(result);
                String resultCode = json.getString(Messages.getString("ChinaUnicom.login.result.resultCode"));
                if (Messages.getString("ChinaUnicom.login.result.resultCode.success").equals(resultCode)) {
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
                    FileUtils.write(createTempFile(BILL_TYPE_PERSONALINFO), result, Charset.forName(HttpUtils.UTF_8),
                            false);
                    // TODO 获取个人信息 判断套餐
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
                    client.close();
                    logger.info("登录失败[" + phone + "," + password + ", " + result + "]");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean loadBills() {
        return this.go();
    }

    /**
     * @author zhuyuhang
     * @return
     */
    private boolean go() {
        this.personalInfo();
        this.hisBill();
        this.gsm();
        this.sms();
        // this.addvalue();
        // this.gprs();
        this.close();
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
        String url = Messages.getString("ChinaUnicom.queryHistoryBill.url") + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put(Messages.getString("ChinaUnicom.queryHistoryBill.param.quertype"),
                Messages.getString("ChinaUnicom.queryHistoryBill.param.quertype.value"));
        params.put(Messages.getString("ChinaUnicom.queryHistoryBill.param.querycode"),
                Messages.getString("ChinaUnicom.queryHistoryBill.param.querycode.value"));
        params.put(Messages.getString("ChinaUnicom.queryHistoryBill.param.billdate"),
                DateUtils.formatDate(date, Messages.getString("ChinaUnicom.queryHistoryBill.param.billdate.format")));
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
    private CU_BJ_MobileFetcher gsm(Date date) {
        gsm(date, 1, 1);
        return this;
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
        logger.info("获取通话详单(" + DateUtils.formatDate(date) + ", " + pageNo + ", " + _totalPages + ")");
        String url = Messages.getString("ChinaUnicom.callDetail.url") + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put(Messages.getString("ChinaUnicom.callDetail.param.pageNo"), pageNo);
        params.put(Messages.getString("ChinaUnicom.callDetail.param.pageSize"), PAGE_SIZE);
        params.put(Messages.getString("ChinaUnicom.callDetail.param.beginDate"), DateUtils.getFirstDayOfMonth(date));
        params.put(Messages.getString("ChinaUnicom.callDetail.param.endDate"), DateUtils.getLastDayOfMonth(date));
        httppost.setEntity(HttpUtils.buildParams(params));
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String r = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            JSONObject json = JSON.parseObject(r);
            boolean isSuccess = json.getBooleanValue(Messages.getString("ChinaUnicom.callDetail.result.isSuccess"));
            if (isSuccess) {
                int totalRecord = json.getIntValue(Messages.getString("ChinaUnicom.callDetail.result.totalRecord"));
                if (totalRecord > 0) {
                    JSONObject pageMap = json
                            .getJSONObject(Messages.getString("ChinaUnicom.callDetail.result.pageMap"));
                    if (pageMap != null) {
                        int totalPages = pageMap.getIntValue(Messages
                                .getString("ChinaUnicom.callDetail.result.pageMap.totalPages"));
                        JSONArray result = pageMap.getJSONArray(Messages
                                .getString("ChinaUnicom.callDetail.result.pageMap.result"));
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
    private void sms(Date date) {
        sms(date, 1, 1);
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
        String url = Messages.getString("ChinaUnicom.sms.url") + System.currentTimeMillis();
        Map<String, Object> params = new HashMap<>();
        params.put(Messages.getString("ChinaUnicom.sms.param.pageNo"), pageNo);
        params.put(Messages.getString("ChinaUnicom.sms.param.pageSize"), PAGE_SIZE);
        params.put(Messages.getString("ChinaUnicom.sms.param.begindate"),
                DateUtils.getFirstDayOfMonth(date, Messages.getString("ChinaUnicom.sms.param.begindate.patter")));
        params.put(Messages.getString("ChinaUnicom.sms.param.enddate"),
                DateUtils.getLastDayOfMonth(date, Messages.getString("ChinaUnicom.sms.param.enddate.pattern")));
        HttpPost httppost = HttpUtils.post(url, params);
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String r = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            JSONObject json = JSON.parseObject(r);
            boolean isSuccess = json.getBooleanValue(Messages.getString("ChinaUnicom.sms.result.isSuccess"));
            if (isSuccess) {
                int totalRecord = json.getIntValue(Messages.getString("ChinaUnicom.sms.result.totalRecord"));
                if (totalRecord > 0) {
                    JSONObject pageMap = json.getJSONObject(Messages.getString("ChinaUnicom.sms.result.pageMap"));
                    if (pageMap != null) {
                        int totalPages = pageMap.getIntValue(Messages
                                .getString("ChinaUnicom.sms.result.pageMap.totalPages"));
                        JSONArray result = pageMap.getJSONArray(Messages
                                .getString("ChinaUnicom.sms.result.pageMap.result"));
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
            final Date date = DateUtils.addMonths(now, -i);
            futures.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    addvalue(date);
                    return null;
                }
            }));
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
        String url = Messages.getString("ChinaUnicom.callValueAdded.url") + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put(Messages.getString("ChinaUnicom.callValueAdded.param.pageNo"), pageNo);
        params.put(Messages.getString("ChinaUnicom.callValueAdded.param.pageSize"), PAGE_SIZE);
        params.put(Messages.getString("ChinaUnicom.callValueAdded.param.beginDate"), DateUtils.getFirstDayOfMonth(date));
        params.put(Messages.getString("ChinaUnicom.callValueAdded.param.endDate"), DateUtils.getLastDayOfMonth(date));
        httppost.setEntity(HttpUtils.buildParams(params));
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String r = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            JSONObject json = JSON.parseObject(r);
            boolean isSuccess = json.getBooleanValue(Messages.getString("ChinaUnicom.callValueAdded.result.isSuccess"));
            if (isSuccess) {
                int totalRecord = json.getIntValue(Messages.getString("ChinaUnicom.callValueAdded.result.totalRecord"));
                if (totalRecord > 0) {
                    JSONObject pageMap = json.getJSONObject(Messages
                            .getString("ChinaUnicom.callValueAdded.result.pageMap"));
                    if (pageMap != null) {
                        int totalPages = pageMap.getIntValue(Messages
                                .getString("ChinaUnicom.callValueAdded.result.pageMap.totalPages"));
                        JSONArray result = pageMap.getJSONArray(Messages
                                .getString("ChinaUnicom.callValueAdded.result.pageMap.result"));
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

    /**
     * 上网流量
     * 
     * @author zhuyuhang
     * @param client
     */
    private void callFlow(Date date) {
        this.checkLogin();
        String url = Messages.getString("ChinaUnicom.callFlow.url") + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put(Messages.getString("ChinaUnicom.callFlow.param.pageNo"), 1);
        params.put(Messages.getString("ChinaUnicom.callFlow.param.pageSize"), PAGE_SIZE);
        params.put(Messages.getString("ChinaUnicom.callFlow.param.beginDate"), DateUtils.getFirstDayOfMonth(date));
        params.put(Messages.getString("ChinaUnicom.callFlow.param.endDate"), DateUtils.getLastDayOfMonth(date));
        httppost.setEntity(HttpUtils.buildParams(params));
        try {
            CloseableHttpResponse response = client.execute(httppost);
            // TODO 官网没有内容 不知道如何解析
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
        for (int i = 0; i < 2; i++) {
            final Date date = DateUtils.addMonths(now, -i);
            futures.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    gprs(date);
                    return null;
                }
            }));
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
        String url = Messages.getString("ChinaUnicom.callNetPlayRecord.url") + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put(Messages.getString("ChinaUnicom.callNetPlayRecord.param.pageNo"), pageNo);
        params.put(Messages.getString("ChinaUnicom.callNetPlayRecord.param.pageSize"), PAGE_SIZE);
        params.put(Messages.getString("ChinaUnicom.callNetPlayRecord.param.beginDate"),
                DateUtils.getFirstDayOfMonth(date));
        params.put(Messages.getString("ChinaUnicom.callNetPlayRecord.param.endDate"), DateUtils.getLastDayOfMonth(date));
        httppost.setEntity(HttpUtils.buildParams(params));
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String r = EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8);
            response.close();
            JSONObject json = JSON.parseObject(r);
            boolean isSuccess = json.getBooleanValue(Messages
                    .getString("ChinaUnicom.callNetPlayRecord.result.isSuccess"));
            if (isSuccess) {
                int totalRecord = json.getIntValue(Messages
                        .getString("ChinaUnicom.callNetPlayRecord.result.totalRecord"));
                if (totalRecord > 0) {
                    JSONObject pageMap = json.getJSONObject(Messages
                            .getString("ChinaUnicom.callNetPlayRecord.result.pageMap"));
                    if (pageMap != null) {
                        int totalPages = pageMap.getIntValue(Messages
                                .getString("ChinaUnicom.callNetPlayRecord.result.pageMap.totalPages"));
                        JSONArray result = pageMap.getJSONArray(Messages
                                .getString("ChinaUnicom.callNetPlayRecord.result.pageMap.result"));
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
    public boolean checkCaptchaCode() {
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
                this.client.close();
            } catch (IOException e) {
                e.printStackTrace();
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

    @Override
    protected void mzlog() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void rc() {
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
