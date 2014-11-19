package com.amos;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.amos.tool.Tools;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Created by lixin on 14-7-21.
 */
public class LoginQunar {

    public static void main(String args[]) throws Exception {
        String username = "去哪儿账号";
        String password = "密码";
        CookieStore cookieStore = new BasicCookieStore();

        CloseableHttpClient httpClient = Tools.createSSLClientDefaultWithCookie(cookieStore);

        //初始化结果
        String result = "";
        String url = "";
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        HttpEntity httpEntity;
        String imageCode = "";

        //取参数
        url = "https://user.qunar.com/passport/login.jsp";
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity());
        Document doc = Jsoup.parse(result);
        String imageCodeUrl = doc.select("#vcodeImg").attr("src");


        //判断是否需要图片验证码
        url = "https://user.qunar.com/webApi/isNeedCaptcha.jsp?username=" + username;
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity());
        //not need {"data":false,"errcode":200,"ret":true,"ver":1}
        //need  {"data":true,"errcode":200,"ret":true,"ver":1}

        if(result.contains("\"data\":false")){
            //开始登录
            url="https://user.qunar.com/passport/loginx.jsp";
            httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            params.add(new BasicNameValuePair("loginType", "0"));
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("remember", "0"));
            params.add(new BasicNameValuePair("vcode",imageCode));
            httpEntity = new UrlEncodedFormEntity(params);
            httpPost.setEntity(httpEntity);
            result = EntityUtils.toString(httpClient.execute(httpPost).getEntity());

        }

        while (result.contains("\"data\":true") || result.contains("验证码错误")) {
            URL imageURL = new URL(imageCodeUrl);
            URI uri = new URI(imageURL.getProtocol(),imageURL.getHost(),imageURL.getPath(),imageURL.getQuery(),null);

            Tools.saveToLocal(httpClient.execute(new HttpGet(uri)).getEntity(),"qunar.png");
            imageCode = JOptionPane.showInputDialog("请输入图片验证码!");

            //开始登录
            url="https://user.qunar.com/passport/loginx.jsp";
            httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            params.add(new BasicNameValuePair("loginType", "0"));
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("remember", "0"));
            params.add(new BasicNameValuePair("vcode",imageCode));
            httpEntity = new UrlEncodedFormEntity(params);
            httpPost.setEntity(httpEntity);
            result = EntityUtils.toString(httpClient.execute(httpPost).getEntity());
        }
        //loginResult:{"data":{"needCaptcha":true},"errcode":11004,"errmsg":"验证码错误","ret":false,"ver":0}
        //{"data":{"needCaptcha":true},"errcode":21022,"errmsg":"用户名或密码错误","ret":false,"ver":0}

        System.out.println("result:"+result);


        //basic
        url="http://user.qunar.com/userinfo/basic.jsp";
        byte[] bytes = EntityUtils.toByteArray(httpClient.execute(new HttpGet(url)).getEntity());

        result = new String(bytes) ;
        System.out.println("result:"+result);
        Tools.saveToLocalByBytes(bytes, "qunar.basic.html");

        String csrfTokenParam = "";
        List<Cookie> cookies = cookieStore.getCookies();
        for(Cookie cookie:cookies){
            if(cookie.getName().contains("csrfToken")){
                csrfTokenParam = cookie.getValue();
            }
        }

        //billist
        url="http://tinfo.qunar.com/order/queryByUser.json?callback";
        params.add(new BasicNameValuePair("showCookie","1"));
        params.add(new BasicNameValuePair("pageSize", "10000"));
        params.add(new BasicNameValuePair("currentPage", "1"));
        params.add(new BasicNameValuePair("csrfTokenParam", csrfTokenParam));
        params.add(new BasicNameValuePair("startDate", "2013-01-22"));
        params.add(new BasicNameValuePair("endDate", "2014-07-22"));
        params.add(new BasicNameValuePair("status", "-1"));
        httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        bytes = EntityUtils.toByteArray(httpClient.execute(httpPost).getEntity());
        result = new String(bytes);
        System.out.println("result:"+result);

        Tools.saveToLocalByBytes(bytes,"qunar.billlist.html");


        //保存详单.
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(result.replace(")","").replace("(",""));
        JsonArray orders = jsonElement.getAsJsonObject().get("list").getAsJsonArray();
        int i=1;
        for(JsonElement order:orders){
            String orderNo = order.getAsJsonObject().get("orderNo").getAsString();
            String ota = order.getAsJsonObject().get("ota").getAsString();
            url="http://tinfo.qunar.com/order/redirect2Detail?orderNo="+orderNo+"&otaType=1&ota="+ota;
            System.out.println("orderNo:"+orderNo);
            Tools.saveToLocalByBytes(bytes,"qunar.bill"+i+".html");
            i++;
            result = new String(bytes);
            System.out.println("result:"+result);

        }

    }
}
