package com.puhui.crawler.test;

import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;

import com.puhui.crawler.util.HttpUtils;

public class HttpClientCookieTest {

    public static void main(String[] args) throws Exception {
        CloseableHttpClient client = HttpUtils.getHttpClient(false);
        CookieStore cookieStore = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        HttpGet get = new HttpGet("http://www.baidu.com");
        CloseableHttpResponse response = client.execute(get, context);
        for (Cookie cookie : cookieStore.getCookies()) {
            System.out.printf("%s\t%s\n", cookie.getName(), cookie.getValue());
        }
        response.close();
    }
}
