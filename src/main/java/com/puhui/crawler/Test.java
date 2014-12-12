package com.puhui.crawler;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.puhui.crawler.util.HttpUtils;

public class Test {
    public static void main(String[] args) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        String url = "https://ipcrs.pbccrc.org.cn/imgrc.do?" + Math.random();
        HttpGet get = HttpUtils.get(url);
        CloseableHttpResponse response = client.execute(get);
        File codeFile = new File("D:/tmp/code.png");
        FileUtils.copyInputStreamToFile(response.getEntity().getContent(), codeFile);
        response.close();

        System.out.println("登录开始");

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        // String loginname = "zhyh1105";
        // String password = "Hxffqqve3002";
        // String tradeCode = "e93yjq";

        String loginname = "lvraikkonen";
        String password = "LvRaikkonen_0306";
        String tradeCode = "mr9aep";
        formparams.add(new BasicNameValuePair("page", "1"));
        formparams.add(new BasicNameValuePair("method", "login"));
        formparams.add(new BasicNameValuePair("date", System.currentTimeMillis() + ""));
        formparams.add(new BasicNameValuePair("loginname", loginname));
        formparams.add(new BasicNameValuePair("password", password));
        formparams.add(new BasicNameValuePair("_@IMGRC@_", Ocr.getCodeFromImage(codeFile, "mycode", true)));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Charset.forName("gbk"));
        HttpPost httppost = new HttpPost("https://ipcrs.pbccrc.org.cn/login.do");
        // httppost.setConfig(config);
        httppost.setEntity(entity);
        response = client.execute(httppost);
        response.close();
        System.out.println("登录结束");

        System.out.println("抓取token开始");
        get = new HttpGet("https://ipcrs.pbccrc.org.cn/simpleReportAction.do?method=welcome");
        // get.setConfig(config);

        response = client.execute(get);
        String html = EntityUtils.toString(response.getEntity());
        response.close();
        Document doc = Jsoup.parse(html);
        Elements els = doc.select("input[name=org.apache.struts.taglib.html.TOKEN]");
        String token = null;
        if (!els.isEmpty()) {
            token = (els.get(0).attr("value"));
        }
        System.out.println("抓取token结束。值:" + token);

        System.out.println("抓取信用报告开始");
        get = new HttpGet("https://ipcrs.pbccrc.org.cn/simpleReportAction.do?method=view&tradeCode=" + tradeCode
                + "&org.apache.struts.taglib.html.TOKEN=" + token);
        // get.setConfig(config);
        System.out.println(get.getURI().toString());
        response = client.execute(get);
        FileUtils.write(new File("D:/tmp/zxbg/ls.html"), EntityUtils.toString(response.getEntity()));
        // System.out.println(EntityUtils.toString(response.getEntity()));
        response.close();
        System.out.println("抓取信用报告结束");
        client.close();
    }
}
