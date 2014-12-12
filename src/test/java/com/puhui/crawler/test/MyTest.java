package com.puhui.crawler.test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.puhui.crawler.util.HttpUtils;

public class MyTest {
    private CloseableHttpClient client = null;
    private HttpGet get = null;
    private HttpPost post = null;
    private CloseableHttpResponse resp = null;

    @Before
    public void before() {
        client = HttpUtils.getHttpClient();
    }

    @Test
    public void testProxy() throws ClientProtocolException, IOException {
        // String url = "http://mytools2.cfapps.io/";
        // get = HttpUtils.get(url);
        // resp = client.execute(get);
        // System.out.println(EntityUtils.toString(resp.getEntity()));
        // resp.close();
        // get = HttpUtils.get(url, HttpUtils.PROXY_FREEGATE);
        // resp = client.execute(get);
        // System.out.println(EntityUtils.toString(resp.getEntity()));
    }

    @Test
    public void testTaobaoMobileApi() throws Exception {
        // for (int i = 0; i < 1000; i++) {
        // String url =
        // "http://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=15029225990";
        // get = HttpUtils.get(url);
        // resp = client.execute(get);
        // System.out.println(EntityUtils.toString(resp.getEntity()));
        // resp.close();
        // }
    }

    @Test
    public void testPackage() {
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource("com/puhui/crawler/test");
            // System.out.println("url = " + url) ;
            File file = new File(new URI(url.toString()));
            String[] names = file.list();
            for (int i = 0; i < names.length; i++) {
                System.out.println("file = " + names[i]);
            }
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    @After
    public void after() throws IOException {
        if (resp != null) {
            resp.close();
        }
        get = null;
        post = null;
        if (client != null) {
            client.close();
        }
    }
}
