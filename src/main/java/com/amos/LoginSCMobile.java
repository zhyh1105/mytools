package com.amos;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.amos.tool.Tools;

/**
 * Created by lixin on 14-7-8.
 */
public class LoginSCMobile {

	public static void main(String args[]) throws Exception {

		String phone = "四川移动";
		String password = "密码";

		HttpClient httpClient = Tools.createSSLClientDefault();

		String loginURL = "https://sc.ac.10086.cn/login/";// sc

		HttpGet httpGet = new HttpGet(loginURL);
		HttpResponse loginResponse = httpClient.execute(httpGet);
		String loginString = EntityUtils.toString(loginResponse.getEntity());
		System.out.println("loginString:\n" + loginString);
		Matcher matcher = Pattern.compile("var keyStr(.*?)function enCode")
				.matcher(loginString.replaceAll("\\r|\\t|\\n|\\a", ""));

		// 使用java截取js方法,首先,将换行符制表符回车符报警符都替换掉,这样在截取时就不会出问题了
		// Matcher matcher =
		// Pattern.compile("initdata=\\{(.*?)\\}</script>").matcher(loginString.replaceAll("\\r|\\t|\\n|\\a",""));

		while (matcher.find()) {
			String jScript = "var keyStr" + matcher.group(1);
			// 第1次加密
			ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
			ScriptEngine scriptEngine = scriptEngineManager
					.getEngineByExtension("js");
			scriptEngine.eval(jScript);
			Invocable invocable = (Invocable) scriptEngine;
			Object encrypt_password = invocable.invokeFunction("encode64",
					password);
			// 第二次加密
			String secondeEncryptURL = "http://www.sc.10086.cn/ssoLogin.do?dispatch=epwd&pwd="
					+ encrypt_password;
			HttpGet secondHttpGet = new HttpGet(secondeEncryptURL);
			HttpResponse secondResponse = httpClient.execute(secondHttpGet);
			encrypt_password = EntityUtils.toString(secondResponse.getEntity());

			// 图片验证码
			String imageLoginURL = "http://www.sc.10086.cn/service/image_login.jsp";
			HttpGet imageLoginGet = new HttpGet(imageLoginURL);
			Tools.saveToLocal(httpClient.execute(imageLoginGet).getEntity(),
					System.currentTimeMillis() + "imageCode.png");
			String imageCode = JOptionPane.showInputDialog("请输入图片验证码:");

			// 开始登录
			String startLoginURL = "http://www.sc.10086.cn/ssoLogin.do";

			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("commend_bunch", ""));
			parameters.add(new BasicNameValuePair("queryEmail", "2"));
			parameters.add(new BasicNameValuePair("loginValue", "SingerLogin"));
			parameters.add(new BasicNameValuePair("dtype", "0"));
			parameters.add(new BasicNameValuePair("pho_nohd", ""));
			parameters.add(new BasicNameValuePair("type_nohd", ""));
			parameters.add(new BasicNameValuePair("dispatch", "ssoLogin"));
			parameters.add(new BasicNameValuePair("pswTypeNew", "1"));
			parameters.add(new BasicNameValuePair("phone_no", phone));
			parameters.add(new BasicNameValuePair("user_passwd",
					encrypt_password.toString()));
			parameters.add(new BasicNameValuePair("fakecode", imageCode));
			parameters.add(new BasicNameValuePair("rememberMe", "on"));

			HttpEntity loginPostEntity = new UrlEncodedFormEntity(parameters);
			HttpPost startLoginPOST = new HttpPost(startLoginURL);
			startLoginPOST.setEntity(loginPostEntity);

			HttpResponse LoginPostResponse = httpClient.execute(startLoginPOST);
			// 登录结果
			String loginResult = EntityUtils.toString(LoginPostResponse
					.getEntity());
			if (!loginResult.contains("验证码不正确")
					&& !loginResult.contains("密码有误")) {
				// 每月账单
				String month = "201405";
				String date_end = "30";
				String monthBillURL = "http://www.sc.10086.cn/historybill.do?dispatch=getHistoryBill&stime="
						+ month;
				HttpPost monthBillPost = new HttpPost(monthBillURL);
				Tools.saveToLocal(
						httpClient.execute(monthBillPost).getEntity(),
						"CHINAMOBILESC.bill." + month + ".html");

				// 详单
				// 1) 发送短信验证码
				String sendSMSURL = "http://www.sc.10086.cn/bill.do";
				List<NameValuePair> smsParameters = new ArrayList<NameValuePair>();
				smsParameters.add(new BasicNameValuePair("dispatch",
						"getBillRandomPwd"));
				smsParameters.add(new BasicNameValuePair("ctype", "2"));
				smsParameters.add(new BasicNameValuePair("month_begin", month));
				smsParameters.add(new BasicNameValuePair("date_end", date_end));
				smsParameters.add(new BasicNameValuePair("call_type", "0"));
				smsParameters.add(new BasicNameValuePair("sec_pwd", ""));
				HttpPost sendSmsHttpPost = new HttpPost(sendSMSURL);
				sendSmsHttpPost.setEntity(new UrlEncodedFormEntity(
						smsParameters));
				String sendResult = EntityUtils.toString(httpClient.execute(
						sendSmsHttpPost).getEntity());
				if (sendResult.length() < 10) {
					// 2)验证短信验证码
					String smsCode = JOptionPane.showInputDialog("请输入短信验证码:");
					String checkSMSURL = "http://www.sc.10086.cn/bill.do?dispatch=checkBillByType&Action=get&ctype=2&month_begin="
							+ month
							+ "&date_begin=1&date_end="
							+ date_end
							+ "&call_type=0&sec_pwd=" + smsCode;
					HttpGet checkSmsCodeGet = new HttpGet(checkSMSURL);
					String checkSmsCodeResult = EntityUtils.toString(httpClient
							.execute(checkSmsCodeGet).getEntity());
					if (checkSmsCodeResult.contains("000")) {
						// 3)获取数据
						String detailURL = "http://www.sc.10086.cn/bill.do?dispatch=getBillByType&ctype=2&month_begin="
								+ month
								+ "&date_begin=1&date_end="
								+ date_end
								+ "&call_type=0&sec_pwd=" + smsCode;
						HttpGet detailHTTPGET = new HttpGet(detailURL);
						detailHTTPGET.getParams().setParameter("Content-Type",
								"application/x-www-form-urlencoded");
						Tools.saveToLocal(httpClient.execute(detailHTTPGET)
								.getEntity(), "detail." + month + ".html");
					}
				}

			}
		}

		httpGet.releaseConnection();

	}
}
