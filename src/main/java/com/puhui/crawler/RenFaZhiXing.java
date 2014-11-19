package com.puhui.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class RenFaZhiXing {

	public static void main(String[] args) throws Exception {

		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		FileInputStream instream = new FileInputStream(new File("D:/tmp/pbccrc.store"));
		try {
			trustStore.load(instream, "123456".toCharArray());
		} finally {
			instream.close();
		}
		
		// Trust own CA and all self-signed certs
		SSLContext sslcontext = SSLContexts.custom()
				.loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
				.build();
		// Allow TLSv1 protocol only
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		
//		CloseableHttpClient client = HttpClients.custom()
//				.setSSLSocketFactory(sslsf).build();
		CloseableHttpClient client = HttpClients.createDefault();


		/*	
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("searchCourtName", "全国法院（包含地方各级法院）"));
			formparams.add(new BasicNameValuePair("selectCourtId", "1"));
			formparams.add(new BasicNameValuePair("selectCourtArrange", "1"));
			formparams.add(new BasicNameValuePair("pname", "朱宇航"));
			formparams.add(new BasicNameValuePair("cardNum", "412724198901204072"));
			formparams.add(new BasicNameValuePair("j_captcha", "12345"));
			formparams.add(new BasicNameValuePair("currentPage", "2"));
//			formparams.add(new BasicNameValuePair("j_captcha", FileUtils
//					.readFileToString(new File("D:/tmp/code.txt"))));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams,Charset.forName("utf-8"));
			HttpPost httppost = new HttpPost("http://zhixing.court.gov.cn/search/search");
//			httppost.setConfig(config);
			httppost.setEntity(entity);
			CloseableHttpResponse res = client.execute(httppost);
			System.out.println(EntityUtils.toString(res.getEntity()));
			if(1 == 1){
				System.out.println("over");
				return;
			}
			System.out.println("登录结束");*/
		 
		 
		HttpHost proxy = new HttpHost("127.0.0.1", 8888, "http");
		RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
		HttpGet get = new HttpGet("https://ipcrs.pbccrc.org.cn/imgrc.do?"+ Math.random());
		get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:32.0) Gecko/20100101 Firefox/32.0");
		//get.setConfig(config);
		CloseableHttpResponse response = client.execute(get);
		FileUtils.copyInputStreamToFile(response.getEntity().getContent(),new File("D:/tmp/code.png"));
		response.close();
		System.out.println("获取验证码结束");

		System.out.println("登录开始");
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("page", "1"));
		formparams.add(new BasicNameValuePair("method", "login"));
		formparams.add(new BasicNameValuePair("date", System.currentTimeMillis() + ""));
		formparams.add(new BasicNameValuePair("loginname", "zuoeryue"));
		formparams.add(new BasicNameValuePair("password", "123456Ph"));
		formparams.add(new BasicNameValuePair("_@IMGRC@_", FileUtils.readFileToString(new File("D:/tmp/code.txt"))));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams,Charset.forName("gbk"));
		HttpPost httppost = new HttpPost("https://ipcrs.pbccrc.org.cn/login.do");
		httppost.setConfig(config);
		httppost.setEntity(entity);
		response = client.execute(httppost);
		response.close();
		System.out.println("登录结束");
		System.out.println("抓取token开始");
		get = new HttpGet("https://ipcrs.pbccrc.org.cn/simpleReportAction.do?method=welcome");
		get.setConfig(config);
		
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
		get = new HttpGet(
				"https://ipcrs.pbccrc.org.cn/simpleReportAction.do?method=view&tradeCode=c397ur&org.apache.struts.taglib.html.TOKEN="
						+ token);
		get.setConfig(config);
		System.out.println(get.getURI().toString());
		response = client.execute(get);
		System.out.println(EntityUtils.toString(response.getEntity()));
		response.close();
		System.out.println("抓取信用报告结束");
		client.close();
	}
}
