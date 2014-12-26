package com.puhui.crawler.subway;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;

import com.puhui.crawler.mobile.MobileFetcher;
import com.puhui.crawler.util.HttpUtils;

public class SubwayTicket extends MobileFetcher {
    private static final Logger logger = Logger.getLogger(SubwayTicket.class);
    private CloseableHttpClient client;

    public SubwayTicket() {
        this.client = HttpUtils.getHttpClient(true);
    }

    public boolean login(String phone, String password, String rnum) {
        try {
            super.login(phone, password, rnum);
            String url = "https://kyfw.12306.cn/otn/login/loginAysnSuggest";
            HttpPost post = HttpUtils.post(url);
            Map<String, Object> params = new HashMap<>();
            params.put("loginUserDTO.user_name", phone);
            params.put("userDTO.password", password);
            params.put("randCode", rnum);
            params.put("randCode_validate", "");
            params.put("NzUyNDg5", "");
            params.put("myversion", "undefined");
            CloseableHttpResponse response = client.execute(post);
            response.close();
            url = "https://kyfw.12306.cn/otn/login/userLogin";
            params = new HashMap<>();
            params.put("_json_att", "");
            post = HttpUtils.post(url, params);
            response = client.execute(post);
            url = HttpUtils.getLocationFromHeader(response, true);// https://kyfw.12306.cn/otn/index/init
            String responseString = HttpUtils.executeGetWithResult(client, url);
            logger.debug(responseString);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public boolean hasCaptcha() {
        return true;
    }

    @Override
    public File loadCaptchaCode() {
        String url = "https://kyfw.12306.cn/otn/passcodeNew/getPassCodeNew?module=login&rand=sjrand";
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
    public boolean loadBills() {
        return false;
    }

    @Override
    public String getIspSimpleName() {

        return null;
    }

    @Override
    public String getAreaSimpleName() {

        return null;
    }

    @Override
    protected void personalInfo() {

    }

    @Override
    protected void currFee() {

    }

    @Override
    protected void hisBill() {

    }

    @Override
    protected void gsm() {

    }

    @Override
    protected void sms() {

    }

    @Override
    protected void mzlog() {

    }

    @Override
    protected void addvalue() {

    }

    @Override
    protected void rc() {

    }

    @Override
    protected void gprs() {

    }

    @Override
    protected void mon() {

    }

}
