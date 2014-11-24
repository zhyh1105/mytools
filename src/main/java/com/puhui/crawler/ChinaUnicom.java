package com.puhui.crawler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puhui.util.DateUtils;
import com.puhui.util.HttpUtils;

public class ChinaUnicom {
    private static final String TMP_FILE = "D:/tmp/bills/10010.txt";
    private static Logger logger = Logger.getLogger(ChinaUnicom.class);

    public static void main(String[] args) throws Exception {
        String username = "18500492821";
        // String password = "100862";
        String password = "100862";
        CloseableHttpClient client = login(username, password);
        // queryAnalysisLogin(client);
        // queryHistoryBill(client);
        // callDetail(client);
        // sms(client);
        // callValueAdded(client);
        // callNetPlayRecord(client);
    }

    /**
     * 登录
     * 
     * @author zhuyuhang
     * @return 返回登录后的session会话
     */
    public static CloseableHttpClient login(String userName, String password) {
        CloseableHttpClient client = HttpClients.createDefault();
        String url = Messages.getString("ChinaUnicom.login.url");
        Map<String, Object> params = new HashMap<>();
        params.put(Messages.getString("ChinaUnicom.login.param.userName"), userName);
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
                String result = EntityUtils.toString(response.getEntity());
                response.close();
                JSONObject json = JSON.parseObject(result);
                String resultCode = json
                        .getString(Messages.getString("ChinaUnicom.login.result.resultCode"));
                if (Messages.getString("ChinaUnicom.login.result.resultCode.success").equals(resultCode)) {
                    logger.info("登录成功[" + userName + "," + password + "]");
                    return client;
                } else {
                    logger.error("登录失败：" + result);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 信息总览
     * 
     * @author zhuyuhang
     * @param client
     */
    public static void queryAnalysisLogin(CloseableHttpClient client) {
        if (client == null) {
            return;
        }
        logger.info("获取信息总览");
        String url = Messages.getString("ChinaUnicom.queryAnalysisLogin.url") + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String resp = EntityUtils.toString(response.getEntity());
            FileUtils.write(new File(TMP_FILE), resp, Charset.forName(HttpUtils.UTF_8), false);
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 前三月的历史账单
     * 
     * @author zhuyuhang
     * @param client
     */
    public static void queryHistoryBill(CloseableHttpClient client) {
        Date date = new Date();
        // 当前月
        queryHistoryBill(client, date);
        // 前一个月
        queryHistoryBill(client, DateUtils.addMonths(date, -1));
        // 前两个月
        queryHistoryBill(client, DateUtils.addMonths(date, -2));
    }

    /**
     * 历史账单
     * 
     * @author zhuyuhang
     * @param client
     */
    public static void queryHistoryBill(CloseableHttpClient client, Date date) {
        if (client == null) {
            return;
        }
        logger.info("获取历史账单(" + DateUtils.formatDate(date) + ")");
        String url = Messages.getString("ChinaUnicom.queryHistoryBill.url") + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put(Messages.getString("ChinaUnicom.queryHistoryBill.param.quertype"),
                Messages.getString("ChinaUnicom.queryHistoryBill.param.quertype.value"));
        params.put(Messages.getString("ChinaUnicom.queryHistoryBill.param.querycode"),
                Messages.getString("ChinaUnicom.queryHistoryBill.param.querycode.value"));
        params.put(
                Messages.getString("ChinaUnicom.queryHistoryBill.param.billdate"),
                DateUtils.formatDate(date,
                        Messages.getString("ChinaUnicom.queryHistoryBill.param.billdate.format")));
        httppost.setEntity(HttpUtils.buildParams(params));
        try {
            CloseableHttpResponse response = client.execute(httppost);
            // TODO 历史账单
            String resp = EntityUtils.toString(response.getEntity());
            FileUtils.write(new File(TMP_FILE), resp, Charset.forName(HttpUtils.UTF_8), true);
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 最近三个月的通话详单
     * 
     * @author zhuyuhang
     * @param client
     */
    public static void callDetail(CloseableHttpClient client) {
        Date date = new Date();
        // 当前月
        callDetail(client, date);
        // 前一个月
        callDetail(client, DateUtils.addMonths(date, -1));
        // 前两个月
        callDetail(client, DateUtils.addMonths(date, -2));

    }

    /**
     * date所在月通话详单
     * 
     * @author zhuyuhang
     * @param client
     * @param date
     */
    public static void callDetail(CloseableHttpClient client, Date date) {
        callDetail(client, date, 1, 1);
    }

    /**
     * 通话详单
     * 
     * @author zhuyuhang
     * @param client
     * @param date
     * @param pageNo
     * @return
     */
    public static void callDetail(CloseableHttpClient client, Date date, int pageNo, int _totalPages) {
        if (client == null || pageNo > _totalPages) {
            return;
        }
        logger.info("获取通话详单(" + DateUtils.formatDate(date) + ", " + pageNo + ", " + _totalPages + ")");
        String url = Messages.getString("ChinaUnicom.callDetail.url") + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put(Messages.getString("ChinaUnicom.callDetail.param.pageNo"), pageNo);
        params.put(Messages.getString("ChinaUnicom.callDetail.param.pageSize"), 20);
        params.put(Messages.getString("ChinaUnicom.callDetail.param.beginDate"),
                DateUtils.modifyToFirstDayOfMonth(date));
        params.put(Messages.getString("ChinaUnicom.callDetail.param.endDate"),
                DateUtils.modifyToLastDayOfMonth(date));
        httppost.setEntity(HttpUtils.buildParams(params));
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String r = EntityUtils.toString(response.getEntity());
            response.close();
            JSONObject json = JSON.parseObject(r);
            boolean isSuccess = json.getBooleanValue(Messages
                    .getString("ChinaUnicom.callDetail.result.isSuccess"));
            if (isSuccess) {
                int totalRecord = json.getIntValue(Messages
                        .getString("ChinaUnicom.callDetail.result.totalRecord"));
                if (totalRecord > 0) {
                    JSONObject pageMap = json.getJSONObject(Messages
                            .getString("ChinaUnicom.callDetail.result.pageMap"));
                    if (pageMap != null) {
                        int totalPages = pageMap.getIntValue(Messages
                                .getString("ChinaUnicom.callDetail.result.pageMap.totalPages"));
                        // TODO 对 result进行处理
                        JSONArray result = pageMap.getJSONArray(Messages
                                .getString("ChinaUnicom.callDetail.result.pageMap.result"));
                        FileUtils.write(new File(TMP_FILE), result.toString(),
                                Charset.forName(HttpUtils.UTF_8), true);
                        callDetail(client, date, ++pageNo, totalPages);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 最近三个月的短信详单
     * 
     * @author zhuyuhang
     * @param client
     */
    public static void sms(CloseableHttpClient client) {
        Date date = new Date();
        // 当前月
        sms(client, date);
        // 前一个月
        sms(client, DateUtils.addMonths(date, -1));
        // 前两个月
        sms(client, DateUtils.addMonths(date, -2));

    }

    /**
     * date所在月短信详单
     * 
     * @author zhuyuhang
     * @param client
     * @param date
     */
    public static void sms(CloseableHttpClient client, Date date) {
        sms(client, date, 1, 1);
    }

    /**
     * 短信详单
     * 
     * @author zhuyuhang
     * @param client
     */
    public static void sms(CloseableHttpClient client, Date date, int pageNo, int _totalPages) {
        if (client == null || pageNo > _totalPages) {
            return;
        }
        logger.info("获取短信详单(" + DateUtils.formatDate(date) + ", " + pageNo + ", " + _totalPages + ")");
        String url = Messages.getString("ChinaUnicom.sms.url") + System.currentTimeMillis();
        Map<String, Object> params = new HashMap<>();
        params.put(Messages.getString("ChinaUnicom.sms.param.pageNo"), pageNo);
        params.put(Messages.getString("ChinaUnicom.sms.param.pageSize"), 20);
        params.put(
                Messages.getString("ChinaUnicom.sms.param.begindate"),
                DateUtils.modifyToFirstDayOfMonth(date,
                        Messages.getString("ChinaUnicom.sms.param.begindate.patter")));
        params.put(
                Messages.getString("ChinaUnicom.sms.param.enddate"),
                DateUtils.modifyToLastDayOfMonth(date,
                        Messages.getString("ChinaUnicom.sms.param.enddate.pattern")));
        HttpPost httppost = HttpUtils.post(url, params);
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String r = EntityUtils.toString(response.getEntity());
            response.close();
            JSONObject json = JSON.parseObject(r);
            boolean isSuccess = json.getBooleanValue(Messages.getString("ChinaUnicom.sms.result.isSuccess"));
            if (isSuccess) {
                int totalRecord = json.getIntValue(Messages.getString("ChinaUnicom.sms.result.totalRecord"));
                if (totalRecord > 0) {
                    JSONObject pageMap = json.getJSONObject(Messages
                            .getString("ChinaUnicom.sms.result.pageMap"));
                    if (pageMap != null) {
                        int totalPages = pageMap.getIntValue(Messages
                                .getString("ChinaUnicom.sms.result.pageMap.totalPages"));
                        // TODO 对 result进行处理
                        JSONArray result = pageMap.getJSONArray(Messages
                                .getString("ChinaUnicom.sms.result.pageMap.result"));
                        FileUtils.write(new File(TMP_FILE), result.toString(),
                                Charset.forName(HttpUtils.UTF_8), true);
                        sms(client, date, ++pageNo, totalPages);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 确认登录状态　
     * 
     * @author zhuyuhang
     * @param client
     */
    public static void checkLogin(CloseableHttpClient client) {
        String url = Messages.getString("ChinaUnicom.checklogin.url") + System.currentTimeMillis();
        HttpPost post = HttpUtils.post(url);
        try {
            client.execute(post).close();
            url = Messages.getString("ChinaUnicom.checkmapExtraParam.url") + System.currentTimeMillis();
            client.execute(post).close();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 最近三个月的增值业务
     * 
     * @author zhuyuhang
     * @param client
     */
    public static void callValueAdded(CloseableHttpClient client) {
        Date date = new Date();
        // 当前月
        callValueAdded(client, date);
        // 前一个月
        callValueAdded(client, DateUtils.addMonths(date, -1));
        // 前两个月
        callValueAdded(client, DateUtils.addMonths(date, -2));

    }

    /**
     * date所在月增值业务
     * 
     * @author zhuyuhang
     * @param client
     * @param date
     */
    public static void callValueAdded(CloseableHttpClient client, Date date) {
        callValueAdded(client, date, 1, 1);
    }

    /**
     * 增值业务
     * 
     * @author zhuyuhang
     * @param client
     */
    public static void callValueAdded(CloseableHttpClient client, Date date, int pageNo, int _totalPages) {
        if (client == null || pageNo > _totalPages) {
            return;
        }
        logger.info("获取增值业务(" + DateUtils.formatDate(date) + ", " + pageNo + ", " + _totalPages + ")");
        String url = Messages.getString("ChinaUnicom.callValueAdded.url") + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put(Messages.getString("ChinaUnicom.callValueAdded.param.pageNo"), pageNo);
        params.put(Messages.getString("ChinaUnicom.callValueAdded.param.pageSize"), 20);
        params.put(Messages.getString("ChinaUnicom.callValueAdded.param.beginDate"),
                DateUtils.modifyToFirstDayOfMonth(date));
        params.put(Messages.getString("ChinaUnicom.callValueAdded.param.endDate"),
                DateUtils.modifyToLastDayOfMonth(date));
        httppost.setEntity(HttpUtils.buildParams(params));
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String r = EntityUtils.toString(response.getEntity());
            response.close();
            JSONObject json = JSON.parseObject(r);
            boolean isSuccess = json.getBooleanValue(Messages
                    .getString("ChinaUnicom.callValueAdded.result.isSuccess"));
            if (isSuccess) {
                int totalRecord = json.getIntValue(Messages
                        .getString("ChinaUnicom.callValueAdded.result.totalRecord"));
                if (totalRecord > 0) {
                    JSONObject pageMap = json.getJSONObject(Messages
                            .getString("ChinaUnicom.callValueAdded.result.pageMap"));
                    if (pageMap != null) {
                        int totalPages = pageMap.getIntValue(Messages
                                .getString("ChinaUnicom.callValueAdded.result.pageMap.totalPages"));
                        // TODO 对 result进行处理
                        JSONArray result = pageMap.getJSONArray(Messages
                                .getString("ChinaUnicom.callValueAdded.result.pageMap.result"));
                        FileUtils.write(new File(TMP_FILE), result.toString(),
                                Charset.forName(HttpUtils.UTF_8), true);
                        callValueAdded(client, date, ++pageNo, totalPages);
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
    public static void callFlow(CloseableHttpClient client, Date date) {
        if (client == null) {
            return;
        }
        String url = Messages.getString("ChinaUnicom.callFlow.url") + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put(Messages.getString("ChinaUnicom.callFlow.param.pageNo"), 1);
        params.put(Messages.getString("ChinaUnicom.callFlow.param.pageSize"), 20);
        params.put(Messages.getString("ChinaUnicom.callFlow.param.beginDate"),
                DateUtils.modifyToFirstDayOfMonth(date));
        params.put(Messages.getString("ChinaUnicom.callFlow.param.endDate"),
                DateUtils.modifyToLastDayOfMonth(date));
        httppost.setEntity(HttpUtils.buildParams(params));
        try {
            CloseableHttpResponse response = client.execute(httppost);
            // TODO 官网没有内容 不知道如何解析
            String resp = (EntityUtils.toString(response.getEntity()));
            FileUtils.write(new File(TMP_FILE), resp, Charset.forName(HttpUtils.UTF_8), true);
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 最近三个月的手机上网记录 貌似只有两个月的数据可查
     * 
     * @author zhuyuhang
     * @param client
     */
    public static void callNetPlayRecord(CloseableHttpClient client) {
        Date date = new Date();
        // 当前月
        callNetPlayRecord(client, date);
        // 前一个月
        callNetPlayRecord(client, DateUtils.addMonths(date, -1));
        // 前两个月
        // callNetPlayRecord(client, DateUtils.addMonths(date, -2));

    }

    /**
     * date所在月手机上网记录
     * 
     * @author zhuyuhang
     * @param client
     * @param date
     */
    public static void callNetPlayRecord(CloseableHttpClient client, Date date) {
        callNetPlayRecord(client, date, 1, 1);
    }

    /**
     * 手机上网记录
     * 
     * @author zhuyuhang
     * @param client
     */
    public static void callNetPlayRecord(CloseableHttpClient client, Date date, int pageNo, int _totalPages) {
        if (client == null || pageNo > _totalPages) {
            return;
        }
        logger.info("获取手机上网记录(" + DateUtils.formatDate(date) + ", " + pageNo + ", " + _totalPages + ")");
        String url = Messages.getString("ChinaUnicom.callNetPlayRecord.url") + System.currentTimeMillis();
        HttpPost httppost = HttpUtils.post(url);
        Map<String, Object> params = new HashMap<>();
        params.put(Messages.getString("ChinaUnicom.callNetPlayRecord.param.pageNo"), pageNo);
        params.put(Messages.getString("ChinaUnicom.callNetPlayRecord.param.pageSize"), 20);
        params.put(Messages.getString("ChinaUnicom.callNetPlayRecord.param.beginDate"),
                DateUtils.modifyToFirstDayOfMonth(date));
        params.put(Messages.getString("ChinaUnicom.callNetPlayRecord.param.endDate"),
                DateUtils.modifyToLastDayOfMonth(date));
        httppost.setEntity(HttpUtils.buildParams(params));
        try {
            CloseableHttpResponse response = client.execute(httppost);
            String r = EntityUtils.toString(response.getEntity());
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
                        // TODO 对 result进行处理
                        JSONArray result = pageMap.getJSONArray(Messages
                                .getString("ChinaUnicom.callNetPlayRecord.result.pageMap.result"));
                        FileUtils.write(new File(TMP_FILE), result.toString(),
                                Charset.forName(HttpUtils.UTF_8), true);
                        callNetPlayRecord(client, date, ++pageNo, totalPages);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
