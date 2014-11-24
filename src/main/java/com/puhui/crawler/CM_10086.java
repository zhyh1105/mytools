package com.puhui.crawler;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.puhui.util.HttpUtils;
import com.puhui.util.SSLUtils;

public class CM_10086 {
	public static void main(String[] args) throws Exception {
		CookieStore cookieStore = new BasicCookieStore();
		// CloseableHttpClient client = HttpUtils.getHttpClient(true,
		// cookieStore);
		File storeFile = new File(
				"D:/tmp/crt/cmodsvr1.bj.chinamobile.com.keystore");
		String storePasswd = "123456";
		CloseableHttpClient client = HttpUtils.getHttpClient(true, cookieStore);
		HttpGet get = null;
		HttpPost post = null;
		CloseableHttpResponse response = null;
		String phone = "13552355914";
		// String password = "zff829";
		String servicePassword = "829928";
		// 登录页面
		String url = "https://bj.ac.10086.cn/login";
		get = HttpUtils.get(url);
		client.execute(get).close();

		url = "https://bj.ac.10086.cn/ac/cmsso/iloginnew.jsp";
		get = HttpUtils.get(url);
		client.execute(get).close();

		cookieStore.addCookie(new BasicClientCookie(" c_mobile", phone));
		cookieStore.addCookie(new BasicClientCookie(" login_mobile", phone));

		// 是否显示验证码
		url = "https://bj.ac.10086.cn/ac/IsShowValidateRnum";
		Map<String, Object> params = new HashMap<>();
		params.put("phone", phone);
		response = client.execute(HttpUtils.post(url, params));
		System.out.println("是否显示验证码:"
				+ EntityUtils.toString(response.getEntity()));
		response.close();

		// BmccMobile
		url = "https://bj.ac.10086.cn/ac/BmccMobile";
		params = new HashMap<>();
		params.put("mobile", phone);
		post = HttpUtils.post(url, params);
		response = client.execute(post);
		String r = EntityUtils.toString(response.getEntity()).trim();
		System.out.println(r);
		response.close();

		// 验证IP
		url = "https://bj.ac.10086.cn/ac/ValidateIp";
		response = client.execute(HttpUtils.post(url));
		System.out.println(EntityUtils.toString(response.getEntity()));
		response.close();

		// 获取验证码
		url = "https://bj.ac.10086.cn/ac/ValidateNum?smartID="
				+ System.currentTimeMillis();

		get = HttpUtils.get(url);
		response = client.execute(get);
		File codeFile = new File("D:/tmp/10086/num/code.jpg");
		FileUtils.copyInputStreamToFile(response.getEntity().getContent(),
				codeFile);
		response.close();

		Desktop.getDesktop().open(codeFile);
		String rnum = JOptionPane.showInputDialog("输入验证码");
		// 验证验证码
		url = "https://bj.ac.10086.cn/ac/ValidateRnum";
		params = new HashMap<>();
		params.put("user", phone);
		params.put("phone", phone);
		params.put("rnum", rnum);
		params.put("service", "www.bj.10086.cn");
		params.put("ssoLogin", "yes");
		params.put("loginMode", "1");
		params.put("loginMethod", "1");

		post = HttpUtils.post(url, params);
		response = client.execute(post);
		System.out.println(EntityUtils.toString(response.getEntity()));
		response.close();

		// 登录
		url = "https://bj.ac.10086.cn/ac/CmSsoLogin?1=1";
		params = new HashMap<>();
		params.put("user", phone);
		params.put("phone", phone);
		params.put("backurl", "http://www.bj.10086.cn/my");
		params.put("continue", "http://www.bj.10086.cn/my");
		params.put("style", "BIZ_LOGINBOX");
		params.put("service", "www.bj.10086.cn");
		params.put("box", null);
		params.put("target", "_parent");
		params.put("ssoLogin", "yes");
		params.put("loginMode", "3");
		params.put("loginMethod", "1");
		params.put("loginName", phone);
		params.put("password", servicePassword);
		params.put("smsNum", "随机码");
		params.put("rnum", rnum);
		params.put("ckCookie", "on");

		post = HttpUtils.post(url, params);
		response = client.execute(post);
		File loginRusultFile = createTempFile();
		HttpEntity entity = response.getEntity();
		writeToFile(loginRusultFile, entity);
		response.close();

		Document document = Jsoup.parse(loginRusultFile, "gb2312");

		Elements elements = document.select("#loginAgain");
		if (!elements.isEmpty()) {
			Element loginAgain = elements.get(0);
			Elements forms = loginAgain.select("div > form");
			if (!forms.isEmpty()) {
				Element form = forms.get(0);
				url = form.attr("action");
				Elements inputs = form.select("input[type=hidden]");
				params = new HashMap<>();
				for (int i = 0; i < inputs.size(); i++) {
					params.put(inputs.get(i).attr("name"),
							inputs.get(i).attr("value"));
				}
				post = HttpUtils.post(url, params);
				client.execute(post).close();
			}
		}

		url = "https://bj.ac.10086.cn/ac/cmsso/redirect.jsp";
		HttpUtils.executeGet(client, url);
		HttpUtils.printCookies(cookieStore);

		url = "http://www.bj.10086.cn/my";
		HttpUtils.executeGet(client, url);

		url = "http://www.bj.10086.cn/service/fee/zdcx/";
		HttpUtils.executeGet(client, url);

		String cmtokenid = HttpUtils.getFirstCookie(cookieStore, "cmtokenid");
		if (cmtokenid == null) {
			return;
		}

		String ssoSessionID = getSSOSessionID(cmtokenid);
		// https://cmodsvr1.bj.chinamobile.com/PortalCMOD/bill/userbilland.do?timer=61&ssoSessionID=2c9d82fa477d6ea30149e23684eb3f0a&Month=2014.10&livel=
		url = "https://cmodsvr1.bj.chinamobile.com/PortalCMOD/bill/userbilland.do?1=1";
		params = new HashMap<String, Object>();
		params.put("timer", 61);
		params.put("ssoSessionID", ssoSessionID);
		params.put("Month", "2014.10");
		params.put("livel", null);
		url += HttpUtils.buildParamString(params);
		get = HttpUtils.get(url);
		// 切换证书
		client = HttpUtils.getHttpClient(SSLUtils
				.createSSLConnectionSocketFactory(storeFile, storePasswd),
				cookieStore);
		response = client.execute(get);
		writeToFile(response.getEntity());
		response.close();

		client.close();
	}

	private static void writeToFile(HttpEntity entity)
			throws UnsupportedEncodingException, ParseException, IOException {
		writeToFile(createTempFile(), entity);
	}

	private static void writeToFile(File file, HttpEntity entity)
			throws UnsupportedEncodingException, ParseException, IOException {
		String encoding = HttpUtils.UTF_8;
		Header contentType = entity.getContentType();
		if (contentType != null) {
			String tmp = parseContentType(contentType.getValue());
			if (tmp != null) {
				encoding = tmp;
			}
		}
		String content = new String(EntityUtils.toString(entity, encoding));
		FileUtils.write(file, content, encoding);
	}

	private static String parseContentType(String contentType) {
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

	private static File createTempFile() {
		return new File("D:/tmp/10086/bills", System.currentTimeMillis()
				+ ".html");
	}

	private static String getSSOSessionID(String cmtokenid) {
		return cmtokenid.split("@")[0];
	}
}
