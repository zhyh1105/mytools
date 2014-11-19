package com.amos.tool;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

import org.apache.http.*;
import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;
import org.apache.http.util.TextUtils;
import org.omg.CORBA.Request;

import com.amos.constants.Constants;

/**
 * Created by lixin on 14-6-25.
 */
public class Tools {


    /**
     * 写文件到本地
     *
     * @param httpEntity
     * @param filename
     */
    public static void saveToLocal(HttpEntity httpEntity, String filename) {

        try {

            File dir = new File(Constants.FATHER_FILE_PATH);
            if (!dir.isDirectory()) {
                dir.mkdir();
            }

            File file = new File(dir.getAbsolutePath() + "/" + filename);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            InputStream inputStream = httpEntity.getContent();

            byte[] bytes = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(bytes)) > 0) {
                fileOutputStream.write(bytes, 0, length);
            }
            inputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 写文件到本地
     *
     * @param bytes
     * @param filename
     */
    public static void saveToLocalByBytes(byte[] bytes, String filename) {

        try {

            File dir = new File(Constants.FATHER_FILE_PATH);
            if (!dir.isDirectory()) {
                dir.mkdir();
            }

            File file = new File(dir.getAbsolutePath() + "/" + filename);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(bytes);
                //fileOutputStream.write(bytes, 0, bytes.length);
                fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 输出
     * @param string
     */
    public static void println(String string){
        System.out.println("string:"+string);
    }
    /**
     * 输出
     * @param string
     */
    public static void printlnerr(String string){
        System.err.println("string:"+string);
    }


    /**
     * 使用ssl通道并设置请求重试处理
     * @return
     */
    public static CloseableHttpClient createSSLClientDefault() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                //信任所有
                public boolean isTrusted(X509Certificate[] chain,String authType) throws CertificateException {
                    return true;
                }
            }).build();

            //自定义的https请求
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

            //设置请求重试处理,重试机制,这里如果请求失败会重试5次
            HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
                @Override
                public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                    if (executionCount >= 5) {
                        // Do not retry if over max retry count
                        return false;
                    }
                    if (exception instanceof InterruptedIOException) {
                        // Timeout
                        return false;
                    }
                    if (exception instanceof UnknownHostException) {
                        // Unknown host
                        return false;
                    }
                    if (exception instanceof ConnectTimeoutException) {
                        // Connection refused
                        return false;
                    }
                    if (exception instanceof SSLException) {
                        // SSL handshake exception
                        return false;
                    }
                    HttpClientContext clientContext = HttpClientContext.adapt(context);
                    HttpRequest request = clientContext.getRequest();
                    boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                    if (idempotent) {
                        // Retry if the request is considered idempotent
                        return true;
                    }
                    return false;
                }
            };

            //请求参数设置,设置请求超时时间为20秒,连接超时为10秒,不允许循环重定向
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(20000).setConnectTimeout(20000)
                    .setCircularRedirectsAllowed(false)
                    .setRedirectsEnabled(false)
                    .build();

            Cookie cookie ;
            return HttpClients.custom().setSSLSocketFactory(sslsf)
                    .setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36")
                    .setMaxConnPerRoute(25).setMaxConnPerRoute(256)
                    .setRetryHandler(retryHandler)
                    .setRedirectStrategy(new SelfRedirectStrategy())
                    .setDefaultRequestConfig(requestConfig)
                    .build();

        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return HttpClients.createDefault();
    }

    /**
     * 带cookiestore
     * @param cookieStore
     * @return
     */

    public static CloseableHttpClient createSSLClientDefaultWithCookie(CookieStore cookieStore) {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                //信任所有
                public boolean isTrusted(X509Certificate[] chain,String authType) throws CertificateException {
                    return true;
                }
            }).build();

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

            //设置请求重试处理,重试机制,这里如果请求失败会重试5次
            HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
                @Override
                public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                    if (executionCount >= 5) {
                        // Do not retry if over max retry count
                        return false;
                    }
                    if (exception instanceof InterruptedIOException) {
                        // Timeout
                        return false;
                    }
                    if (exception instanceof UnknownHostException) {
                        // Unknown host
                        return false;
                    }
                    if (exception instanceof ConnectTimeoutException) {
                        // Connection refused
                        return false;
                    }
                    if (exception instanceof SSLException) {
                        // SSL handshake exception
                        return false;
                    }
                    HttpClientContext clientContext = HttpClientContext.adapt(context);
                    HttpRequest request = clientContext.getRequest();
                    boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                    if (idempotent) {
                        // Retry if the request is considered idempotent
                        return true;
                    }
                    return false;
                }
            };

            //请求参数设置,设置请求超时时间为20秒,连接超时为10秒,不允许循环重定向
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(20000).setConnectTimeout(20000)
                    .setCircularRedirectsAllowed(false)
                    .build();


            return HttpClients.custom().setSSLSocketFactory(sslsf)
                    .setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36")
                    .setMaxConnPerRoute(25).setMaxConnPerRoute(256)
                    .setRetryHandler(retryHandler)
                    .setRedirectStrategy(new SelfRedirectStrategy())
                    .setDefaultRequestConfig(requestConfig)
                    .setDefaultCookieStore(cookieStore)
                    .build();

        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return HttpClients.createDefault();
    }


    /**
     * 提取数字
     * @param value
     * @return
     */
    public static String GetNumber(String value){
        return value.replaceAll("\\D", "");
    }

//    Pattern compile = Pattern.compile("\\d+\\.\\d+");
//    Matcher matcher = compile.matcher(abc);
//    matcher.find();
//    String string = matcher.group();//提取匹配到的结果
//    System.out.println(string);//0.00

}
