package com.puhui.crawler.test;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.puhui.crawler.util.HttpUtils;

public class CmbChinaTest {

    public static void main(String[] args) throws Exception {
        CloseableHttpClient client = HttpUtils.getHttpClient(true);
        Map<String, Object> params = new HashMap<>();
        params.put("DeviceType", "E");
        params.put("Version", "3.0.0");
        params.put("SystemVersion", "4.4.4");
        params.put("ClientCRC", "E1FBBCEC");
        params.put("FinancialMenuHash", "C09A9FC3F33E133795DDC8BB8E7A120E");
        params.put("MenuHash", "68B288100C698A8EFACBB573F2125FAE");
        params.put("AppID", "0024430000020141014214006aaa0426027940000000000000000000BDIu8lc=");
        HttpPost post = HttpUtils.post("https://mobile.cmbchina.com:443/MobileHtml/Login/GetClientCfg.aspx", params);
        // post.removeHeaders("User-Agent");
        post.setHeader("User-Agent", null);
        post.setHeader("Accept-Encoding", "gzip");
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        client.execute(post).close();

        params = new HashMap<>();
        params.put("Command", "PRE_LOGIN");
        params.put("DeviceType", "E");
        params.put("Version", "3.0.0");
        params.put("SystemVersion", "4.4.4");
        params.put("extraFormat", "PNG");
        params.put("NeedExtraWord", "Y");
        post = HttpUtils.post("https://mobile.cmbchina.com/MobileHtml/Login/Client/LoginClient.aspx", params);
        post.setHeader("User-Agent", "");
        post.setHeader("Accept-Encoding", "gzip");
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        client.execute(post).close();

        params = new HashMap<>();
        params.put("ClientNo", "9E1005D603875D13F08B40644253B692465988828347482300172130");
        params.put("Version", "3.0.0");
        params.put("SystemVersion", "4.4.4");
        params.put("AppID", "0024430000020141014214006aaa0426027940000000000000000000BDIu8lc=");
        params.put("ClientCRC", "E1FBBCEC");
        params.put("FinancialMenuHash", "C09A9FC3F33E133795DDC8BB8E7A120E");
        params.put("MenuHash", "68B288100C698A8EFACBB573F2125FAE");
        params.put("command", "A");
        params.put("AreaNo", null);
        params.put("AccountNo", "6225880146910717");
        params.put("Pwd", "AhXoP2mTqhF0xAOvrXlgFo2jdc4IvCL8WGNYJ2XfpvYJeQRIkXsDzR11w3wcLO*MSZ4*7zhaRJSzV5K2zOeMQw__");
        params.put("ExtraPwd", "0299");
        post = HttpUtils.post("https://mobile.cmbchina.com/MobileHtml/Login/Client/LoginClient.aspx", params);
        CloseableHttpResponse response = client.execute(post);
        System.out.println(EntityUtils.toString(response.getEntity()));
        response.close();
        client.close();
    }

}
