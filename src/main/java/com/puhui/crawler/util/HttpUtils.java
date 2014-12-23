package com.puhui.crawler.util;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HttpUtils {
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:32.0) Gecko/20100101 Firefox/33.0";
    public static final String UTF_8 = "UTF-8";
    public static final String GBK = "GBK";
    public static final HttpHost PROXY_FIDDLER = new HttpHost("127.0.0.1", 8888, "http");
    public static final HttpHost PROXY_FREEGATE = new HttpHost("127.0.0.1", 8580, "http");
    public static final HttpHost PROXY_NULL = new HttpHost("127.0.0.1", 8888, "http");
    // private static RequestConfig DEFAULT_REQUEST_CONFIG =
    // RequestConfig.custom().setRedirectsEnabled(false)
    // .setRelativeRedirectsAllowed(false).setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();
    public static RequestConfig DEFAULT_REQUEST_CONFIG = RequestConfig.custom().setProxy(PROXY_FIDDLER)
            .setRedirectsEnabled(false).setRelativeRedirectsAllowed(false).setCookieSpec(CookieSpecs.BEST_MATCH)
            .build();
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
        return post(url, params, null);
    }

    public static HttpPost post(String url, Map<String, Object> params, HttpHost proxy) {
        HttpPost result = new HttpPost(url);
        result.addHeader("User-Agent", DEFAULT_USER_AGENT);
        if (params != null && !params.isEmpty()) {
            result.setEntity(buildParams(params));
        }
        RequestConfig.Builder builder = copyDefaultConfig();
        if (proxy != null) {
            builder.setProxy(PROXY_FIDDLER);
        }
        result.setConfig(builder.build());
        return result;
    }

    public static HttpGet get(String url) {
        return get(url, null);
    }

    public static HttpGet get(String url, HttpHost proxy) {
        HttpGet result = new HttpGet(url);
        result.addHeader("User-Agent", DEFAULT_USER_AGENT);
        RequestConfig.Builder builder = copyDefaultConfig();
        if (proxy != null) {
            builder.setProxy(proxy);
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

    @SuppressWarnings("rawtypes")
    public static UrlEncodedFormEntity buildParams(Map<String, Object> params, String encoding) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        for (Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                if (value instanceof List) {
                    for (Object o : (List) value) {
                        if (o != null) {
                            parameters.add(new BasicNameValuePair(entry.getKey(), o.toString()));
                        }
                    }
                } else {
                    parameters.add(new BasicNameValuePair(entry.getKey(), value.toString()));
                }
            } else {
                parameters.add(new BasicNameValuePair(entry.getKey(), null));
            }
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

    public static String executeGetWithResult(CloseableHttpClient client, String url) throws ClientProtocolException,
            IOException {
        HttpGet get = get(url);
        CloseableHttpResponse resp = client.execute(get);
        String result = EntityUtils.toString(resp.getEntity());
        resp.close();
        return result;
    }

    public static void executePost(CloseableHttpClient client, String url) throws ClientProtocolException, IOException {
        executePost(client, url, null);
    }

    public static String executePostWithResult(CloseableHttpClient client, HttpPost post)
            throws ClientProtocolException, IOException {
        CloseableHttpResponse resp = client.execute(post);
        String result = EntityUtils.toString(resp.getEntity());
        resp.close();
        return result;
    }

    public static String executePostWithResult(CloseableHttpClient client, String url, Map<String, Object> params)
            throws ClientProtocolException, IOException {
        HttpPost post = params == null ? post(url) : post(url, params);
        CloseableHttpResponse resp = client.execute(post);
        String result = EntityUtils.toString(resp.getEntity());
        resp.close();
        return result;
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
            System.out.println(cookie.toString());
        }
    }

    public static HttpPost buildPostFromHtml(String html) {
        return buildPostFromHtml(html, "form");
    }

    public static HttpPost buildPostFromHtml(String html, String selector) {
        return buildPostFromHtml(html, selector, HttpUtils.GBK);
    }

    public static HttpPost buildPostFromHtml(String html, String selector, String charSet) {
        Document document = Jsoup.parse(html, charSet == null ? HttpUtils.GBK : charSet);
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

    public static Map<String, Object> getFormUrlAndParamsFromHtml(String html, String selector) {
        return getFormUrlAndParamsFromHtml(html, selector, HttpUtils.GBK);
    }

    public static Map<String, Object> getFormUrlAndParamsFromHtml(String html, String selector, String charSet) {
        Document document = Jsoup.parse(html, charSet == null ? HttpUtils.GBK : charSet);
        Elements elements = document.select(selector);
        if (elements.size() > 0) {
            Element form = elements.get(0);
            String url = form.attr("action");
            Elements inputs = form.select("input[type=hidden]");
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < inputs.size(); i++) {
                params.put(inputs.get(i).attr("name"), inputs.get(i).attr("value"));
            }
            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("params", params);
            return result;
        }
        return null;
    }

    /**
     * 获取input[type=hidden]
     * 
     * @author zhuyuhang
     * @param html
     * @return
     */
    public static Map<String, Object> buildHiddenInputParamsFromHtml(String html) {
        return buildHiddenInputParamsFromHtml(html, HttpUtils.GBK);
    }

    /**
     * 获取input[type=hidden]
     * 
     * @author zhuyuhang
     * @param html
     * @param charSet
     * @return
     */
    public static Map<String, Object> buildHiddenInputParamsFromHtml(String html, String charSet) {
        Document document = Jsoup.parse(html, charSet == null ? HttpUtils.GBK : charSet);
        Elements inputs = document.select("input[type=hidden]");
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < inputs.size(); i++) {
            String name = inputs.get(i).attr("name");
            String value = inputs.get(i).attr("value");
            if (params.get(name) != null) {
                Object v = params.get(name);
                if (v instanceof List) {
                    ((List<Object>) v).add(value);
                } else {
                    List<Object> l = new ArrayList<>();
                    l.add(v);
                    l.add(value);
                    params.put(name, l);
                }
            } else {
                params.put(name, value);
            }
        }
        return params;
    }

    public static Map<String, Object> buildParamsFromHtml(String html, String selector) {
        return buildParamsFromHtml(html, selector, HttpUtils.GBK);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> buildParamsFromHtml(String html, String selector, String charSet) {
        Document document = Jsoup.parse(html, charSet == null ? HttpUtils.GBK : charSet);
        Elements elements = document.select(selector);
        if (elements.size() > 0) {
            Element form = elements.get(0);
            Elements inputs = form.select("input[type=hidden]");
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < inputs.size(); i++) {
                String name = inputs.get(i).attr("name");
                String value = inputs.get(i).attr("value");
                if (params.get(name) != null) {
                    Object v = params.get(name);
                    if (v instanceof List) {
                        ((List<Object>) v).add(value);
                    } else {
                        List<Object> l = new ArrayList<>();
                        l.add(v);
                        l.add(value);
                        params.put(name, l);
                    }
                } else {
                    params.put(name, value);
                }
            }
            return params;
        }
        return new HashMap<>();
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

    /**
     * @author zhuyuhang
     * @param response
     * @param name
     * @param encode
     * @return
     */
    public static String getHeader(CloseableHttpResponse response, String name) {
        Header[] headers = response.getHeaders(name);
        if (headers.length > 0) {
            return headers[0].getValue();
        }
        return null;
    }

    /**
     * 从header里获取Location
     * 
     * @author zhuyuhang
     * @param response
     * @return
     */
    public static String getLocationFromHeader(CloseableHttpResponse response) {
        return getLocationFromHeader(response, false);
    }

    /**
     * @author zhuyuhang
     * @param name
     * @param value
     * @param path
     * @param domain
     * @return
     */
    public static BasicClientCookie getCookie(String name, String value, String domain, String path) {
        BasicClientCookie clientCookie = new BasicClientCookie(name, value);
        clientCookie.setDomain(domain);
        clientCookie.setPath(path);
        return clientCookie;
    }

    public static String getLocationFromHeader(CloseableHttpResponse response, boolean close) {
        String result = getHeader(response, "Location");
        if (close) {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static final Pattern RE_UNICODE = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");

    public static String unicodeToString(String s) {
        Matcher m = RE_UNICODE.matcher(s);
        StringBuffer sb = new StringBuffer(s.length());
        while (m.find()) {
            m.appendReplacement(sb, Character.toString((char) Integer.parseInt(m.group(1), 16)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

}
