package com.puhui.crawler.test;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.puhui.crawler.util.HttpUtils;

public class TestFindMobile {

    public static void main(String[] args) throws Exception {
        CloseableHttpClient client = HttpUtils.getHttpClient();
        for (int i = 0; i < 1000; i++) {
            HttpGet get = HttpUtils
                    .get("http://v.showji.com/locating/showji.com1118.aspx?m=18500492821&output=json&timestamp="
                            + System.currentTimeMillis());
            CloseableHttpResponse response = client.execute(get);
            System.out.printf("%d\t%s\n", i, EntityUtils.toString(response.getEntity(), HttpUtils.UTF_8));
            response.close();
            Thread.sleep(1000);
        }
        client.close();
    }
}
