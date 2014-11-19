package com.puhui.util;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class HttpUtils {
	public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:32.0) Gecko/20100101 Firefox/33.0";
	public static final String DEFAULT_ENCODING = "UTF-8";
	private static final HttpHost proxy = new HttpHost("127.0.0.1", 8888,
			"http");
	private static final RequestConfig config = RequestConfig.custom()
			.setProxy(proxy).build();
	private static SSLContext sslContext = null;
	static {
		try {
			sslContext = new SSLContextBuilder().loadTrustMaterial(null,
					new TrustStrategy() {
						// 信任所有
						@Override
						public boolean isTrusted(X509Certificate[] chain,
								String authType) throws CertificateException {
							return true;
						}

					}).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
			sslContext);

	public static HttpPost post(String url) {
		return post(url, null);
	}

	public static HttpPost post(String url, Map<String, Object> params) {
		return post(url, params, true);
	}

	public static HttpPost post(String url, Map<String, Object> params,
			boolean proxy) {
		HttpPost post = new HttpPost(url);
		post.addHeader("User-Agent", DEFAULT_USER_AGENT);
		if (params != null && !params.isEmpty()) {
			post.setEntity(buildParams(params));
		}
		if (proxy) {
			post.setConfig(config);
		}
		return post;
	}

	public static HttpGet get(String url) {
		return get(url, false);
	}

	public static HttpGet get(String url, boolean proxy) {
		HttpGet get = new HttpGet(url);
		get.addHeader("User-Agent", DEFAULT_USER_AGENT);
		if (proxy) {
			get.setConfig(config);
		}
		return get;
	}

	public static UrlEncodedFormEntity buildParams(Map<String, Object> params) {
		return buildParams(params, DEFAULT_ENCODING);
	}

	public static UrlEncodedFormEntity buildParams(Map<String, Object> params,
			String encoding) {
		if (params == null || params.isEmpty()) {
			return null;
		}
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		for (Entry<String, Object> entry : params.entrySet()) {
			Object value = entry.getValue();
			value = value == null ? null : value.toString();
			parameters.add(new BasicNameValuePair(entry.getKey(),
					(String) value));
		}
		return new UrlEncodedFormEntity(parameters, Charset.forName(encoding));
	}

	public static String buildParamString(Map<String, Object> params) {
		return buildParamString(params, DEFAULT_ENCODING);
	}

	public static String buildParamString(Map<String, Object> params,
			String encoding) {
		StringBuilder sb = new StringBuilder();
		try {
			for (Entry<String, Object> entry : params.entrySet()) {
				Object value = entry.getValue();
				value = value == null ? null : value.toString();
				sb.append("&").append(entry.getKey()).append("=")
						.append(URLEncoder.encode((String) value, encoding));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static CloseableHttpClient getHttpClient(boolean ssl) {
		if (ssl) {
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		}
		return HttpClients.createDefault();
	}

	public static void main(String[] args) {
		Calendar calendar = Calendar.getInstance();
		System.out.println(calendar.getMaximum(Calendar.DAY_OF_MONTH));
		System.out.println(calendar.get(Calendar.DATE));
	}
}
