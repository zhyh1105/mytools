package com.puhui.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class GetImages {
    // https://ipcrs.pbccrc.org.cn/imgrc.do?0.4631039112122671
    public static void main(String[] args) throws Exception {
        getCodeImage(100);
    }

    public static void getCodeImage(int num) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream instream = new FileInputStream(new File("D:/tmp/pbccrc.store"));
        try {
            trustStore.load(instream, "123456".toCharArray());
        } finally {
            instream.close();
        }

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

        // CloseableHttpClient client =
        // HttpClients.custom().setSSLSocketFactory(sslsf).build();
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet("http://www.189.cn/dqmh/createCheckCode.do?method=checkLoginCode&date="
                + Math.random());
        get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:32.0) Gecko/20100101 Firefox/32.0");
        for (int i = 0; i < num; i++) {
            File file = new File("D:/tmp/images2/" + i + ".jpg");
            CloseableHttpResponse response = client.execute(get);
            FileUtils.copyInputStreamToFile(response.getEntity().getContent(), file);
            response.close();
            System.out.println(file.getAbsolutePath());
            Thread.sleep(100);
        }
    }
}
