package com.puhui.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HttpUtils {
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:32.0) Gecko/20100101 Firefox/33.0";
    public static final String UTF_8 = "UTF-8";
    public static final String GBK = "GBK";
    private static final HttpHost PROXY_HOST = new HttpHost("127.0.0.1", 8888, "http");
    private static RequestConfig DEFAULT_REQUEST_CONFIG = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();
    private static SSLContext sslContext = null;
    static {
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                // 信任所有
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }

            }).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final static SSLConnectionSocketFactory DEFAULT_SSLSF = new SSLConnectionSocketFactory(sslContext);

    public static HttpPost post(String url) {
        return post(url, null);
    }

    public static HttpPost post(String url, Map<String, Object> params) {
        return post(url, params, true);
    }

    public static HttpPost post(String url, Map<String, Object> params, boolean proxy) {
        HttpPost result = new HttpPost(url);
        result.addHeader("User-Agent", DEFAULT_USER_AGENT);
        if (params != null && !params.isEmpty()) {
            result.setEntity(buildParams(params));
        }
        RequestConfig.Builder builder = copyDefaultConfig();
        if (proxy) {
            builder.setProxy(PROXY_HOST);
        }
        result.setConfig(builder.build());
        return result;
    }

    public static HttpGet get(String url) {
        return get(url, false);
    }

    public static HttpGet get(String url, boolean proxy) {
        HttpGet result = new HttpGet(url);
        result.addHeader("User-Agent", DEFAULT_USER_AGENT);
        RequestConfig.Builder builder = copyDefaultConfig();
        if (proxy) {
            builder.setProxy(PROXY_HOST);
        }
        result.setConfig(builder.build());
        return result;
    }

    public static RequestConfig.Builder copyDefaultConfig() {
        return RequestConfig.copy(DEFAULT_REQUEST_CONFIG);
    }

    public static UrlEncodedFormEntity buildParams(Map<String, Object> params) {
        return buildParams(params, UTF_8);
    }

    public static UrlEncodedFormEntity buildParams(Map<String, Object> params, String encoding) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        for (Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            value = value == null ? null : value.toString();
            parameters.add(new BasicNameValuePair(entry.getKey(), (String) value));
        }
        return new UrlEncodedFormEntity(parameters, Charset.forName(encoding));
    }

    public static String buildParamString(Map<String, Object> params) {
        return buildParamString(params, UTF_8);
    }

    public static String buildParamString(Map<String, Object> params, String encoding) {
        StringBuilder sb = new StringBuilder();
        try {
            for (Entry<String, Object> entry : params.entrySet()) {
                Object value = entry.getValue();
                value = value == null ? "" : value.toString();
                sb.append("&").append(entry.getKey()).append("=").append(URLEncoder.encode((String) value, encoding));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static CloseableHttpClient getHttpClient() {
        return getHttpClient(false, null);
    }

    public static CloseableHttpClient getHttpClient(boolean ssl) {
        return getHttpClient(ssl, null);
    }

    public static CloseableHttpClient getHttpClient(SSLConnectionSocketFactory sslcsf, CookieStore cookieStore) {
        HttpClientBuilder builder = HttpClients.custom();
        if (cookieStore != null) {
            builder.setDefaultCookieStore(cookieStore);
        }
        builder.setSSLSocketFactory(sslcsf);
        return builder.build();
    }

    public static CloseableHttpClient getHttpClient(boolean ssl, CookieStore cookieStore) {
        HttpClientBuilder builder = HttpClients.custom();
        if (cookieStore != null) {
            builder.setDefaultCookieStore(cookieStore);
        }
        if (ssl) {
            builder.setSSLSocketFactory(DEFAULT_SSLSF);
        }
        return builder.build();
    }

    public static void executeGet(CloseableHttpClient client, String url) throws ClientProtocolException, IOException {
        HttpGet get = HttpUtils.get(url);
        client.execute(get).close();
    }

    public static void executePost(CloseableHttpClient client, String url) throws ClientProtocolException, IOException {
        executePost(client, url, null);
    }

    public static void executePost(CloseableHttpClient client, String url, Map<String, Object> params)
            throws ClientProtocolException, IOException {
        HttpPost post = params == null ? post(url) : post(url, params);
        client.execute(post).close();
    }

    public static String getFirstCookie(CookieStore cookieStore, String name) {
        List<String> values = getCookie(cookieStore, name);
        return values.isEmpty() ? null : values.get(0);
    }

    public static List<String> getCookie(CookieStore cookieStore, String name) {
        List<String> result = new ArrayList<>();
        if (cookieStore == null) {
            return result;
        }
        for (Cookie cookie : cookieStore.getCookies()) {
            if (name.equals(cookie.getName())) {
                result.add(cookie.getValue());
            }
        }
        return result;
    }

    public static void printCookies(CookieStore cookieStore) {
        for (Cookie cookie : cookieStore.getCookies()) {
            System.out.printf("%s\t%s\n", cookie.getName(), cookie.getValue());
        }
    }

    public static HttpPost buildPostFromHtml(String html) {
        return buildPostFromHtml(html, "form");
    }

    public static HttpPost buildPostFromHtml(String html, String selector) {
        Document document = Jsoup.parse(html, HttpUtils.GBK);
        Elements elements = document.select(selector);
        if (elements.size() > 0) {
            Element form = elements.get(0);
            String url = form.attr("action");
            Elements inputs = form.select("input[type=hidden]");
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < inputs.size(); i++) {
                params.put(inputs.get(i).attr("name"), inputs.get(i).attr("value"));
            }
            return HttpUtils.post(url, params);
        }
        return null;
    }

    public static String getCharsetFromContentType(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return null;
        }
        String[] cts = contentType.toLowerCase().split(";");
        for (String s : cts) {
            if (StringUtils.isNotBlank(s) && s.contains("charset")) {
                return s.split("=")[1];
            }
        }
        return null;
    }
}
