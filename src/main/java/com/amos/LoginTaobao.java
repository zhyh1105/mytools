package com.amos;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.amos.tool.HttpParser;
import com.amos.tool.Tools;

/**
 * @ClassName: LoginTaobao
 * @Description: 登录淘宝，目前支持中英文用户名，不支持需要二次验证的账号
 * @author: Created by lixin on 
 * @email:Created by lixin on @infomorrow.com
 * @date 2014年7月7日 下午5:13:22
 */
public class LoginTaobao {
	private static HttpParser httpParser = new HttpParser();

	public static void main(String[] args) throws Exception {

		String TPL_username = "lixinainifly";
		String TPL_password = "0621102/";

		long StartTime = System.currentTimeMillis();
		System.out.println("StartTime:" + StartTime);

		HttpClient httpClient = Tools.createSSLClientDefault();

		String loginUrl = "https://login.taobao.com/member/login.jhtml";

		HttpGet httpGet = new HttpGet(loginUrl);

		String loginHTML = EntityUtils.toString(httpClient.execute(httpGet).getEntity());

		// 拼接post参数
		String loginType = httpParser.getValueFromInputByName(loginHTML, "loginType");
		String TPL_checkcode = httpParser.getValueFromInputByName(loginHTML, "TPL_checkcode");
		String loginsite = httpParser.getValueFromInputByName(loginHTML, "loginsite");
		String newlogin = httpParser.getValueFromInputByName(loginHTML, "newlogin");
		String TPL_redirect_url = httpParser.getValueFromInputByName(loginHTML, "TPL_redirect_url");
		String from = httpParser.getValueFromInputByName(loginHTML, "from");
		String fc = httpParser.getValueFromInputByName(loginHTML, "fc");
		String style = httpParser.getValueFromInputByName(loginHTML, "style");
		String css_style = httpParser.getValueFromInputByName(loginHTML, "css_style");
		String tid = httpParser.getValueFromInputByName(loginHTML, "tid");
		String support = httpParser.getValueFromInputByName(loginHTML, "support");
		String CtrlVersion = httpParser.getValueFromInputByName(loginHTML, "CtrlVersion");
		String minititle = httpParser.getValueFromInputByName(loginHTML, "minititle");
		String minipara = httpParser.getValueFromInputByName(loginHTML, "minipara");
		String umto = httpParser.getValueFromInputByName(loginHTML, "umto");
		String pstrong = httpParser.getValueFromInputByName(loginHTML, "pstrong");
		String llnick = httpParser.getValueFromInputByName(loginHTML, "llnick");
		String sign = httpParser.getValueFromInputByName(loginHTML, "sign");
		String need_sign = httpParser.getValueFromInputByName(loginHTML, "need_sign");
		String isIgnore = httpParser.getValueFromInputByName(loginHTML, "isIgnore");
		String full_redirect = httpParser.getValueFromInputByName(loginHTML, "full_redirect");
		String popid = httpParser.getValueFromInputByName(loginHTML, "popid");
		String callback = httpParser.getValueFromInputByName(loginHTML, "callback");
		String guf = httpParser.getValueFromInputByName(loginHTML, "guf");
		String not_duplite_str = httpParser.getValueFromInputByName(loginHTML, "not_duplite_str");
		String need_user_id = httpParser.getValueFromInputByName(loginHTML, "need_user_id");
		String poy = httpParser.getValueFromInputByName(loginHTML, "poy");
		String gvfdcname = httpParser.getValueFromInputByName(loginHTML, "gvfdcname");
		String gvfdcre = httpParser.getValueFromInputByName(loginHTML, "gvfdcre");
		String from_encoding = httpParser.getValueFromInputByName(loginHTML, "from_encoding");
		String sub = httpParser.getValueFromInputByName(loginHTML, "sub");
		String allp = httpParser.getValueFromInputByName(loginHTML, "allp");
		String osVer = httpParser.getValueFromInputByName(loginHTML, "osVer");

		List<BasicNameValuePair> loginParameters = new ArrayList<BasicNameValuePair>();
		loginParameters.add(new BasicNameValuePair("loginsite", loginsite));
		loginParameters.add(new BasicNameValuePair("TPL_username", TPL_username));
		loginParameters.add(new BasicNameValuePair("TPL_password", TPL_password));
		loginParameters.add(new BasicNameValuePair("TPL_checkcode", TPL_checkcode));
		loginParameters.add(new BasicNameValuePair("loginsite", loginsite));
		loginParameters.add(new BasicNameValuePair("newlogin", newlogin));
		loginParameters.add(new BasicNameValuePair("TPL_redirect_url", TPL_redirect_url));
		loginParameters.add(new BasicNameValuePair("from", from));
		loginParameters.add(new BasicNameValuePair("fc", fc));
		loginParameters.add(new BasicNameValuePair("style", style));
		loginParameters.add(new BasicNameValuePair("css_style", css_style));
		loginParameters.add(new BasicNameValuePair("tid", tid));
		loginParameters.add(new BasicNameValuePair("support", support));
		loginParameters.add(new BasicNameValuePair("CtrlVersion", CtrlVersion));
		loginParameters.add(new BasicNameValuePair("loginType", loginType));
		loginParameters.add(new BasicNameValuePair("minititle", minititle));
		loginParameters.add(new BasicNameValuePair("minipara", minipara));
		loginParameters.add(new BasicNameValuePair("umto", umto));
		loginParameters.add(new BasicNameValuePair("pstrong", pstrong));
		loginParameters.add(new BasicNameValuePair("llnick", llnick));
		loginParameters.add(new BasicNameValuePair("sign", sign));
		loginParameters.add(new BasicNameValuePair("need_sign", need_sign));
		loginParameters.add(new BasicNameValuePair("isIgnore", isIgnore));
		loginParameters.add(new BasicNameValuePair("full_redirect", full_redirect));
		loginParameters.add(new BasicNameValuePair("popid", popid));
		loginParameters.add(new BasicNameValuePair("callback", callback));
		loginParameters.add(new BasicNameValuePair("guf", guf));
		loginParameters.add(new BasicNameValuePair("not_duplite_str", not_duplite_str));
		loginParameters.add(new BasicNameValuePair("need_user_id", need_user_id));
		loginParameters.add(new BasicNameValuePair("poy", poy));
		loginParameters.add(new BasicNameValuePair("gvfdcname", gvfdcname));
		loginParameters.add(new BasicNameValuePair("gvfdcre", gvfdcre));
		loginParameters.add(new BasicNameValuePair("from_encoding", from_encoding));
		loginParameters.add(new BasicNameValuePair("sub", sub));
		loginParameters.add(new BasicNameValuePair("allp", allp));
		loginParameters.add(new BasicNameValuePair("oslanguage", "en-US"));
		loginParameters.add(new BasicNameValuePair("sr", "1280*800"));
		loginParameters.add(new BasicNameValuePair("osVer", osVer));
		loginParameters.add(new BasicNameValuePair("naviVer", "firefox|30"));

		// 登录的最终结果
		String loginResult = "";
		HttpPost loginHttpPost = new HttpPost(loginUrl);
		// 这里设置一下参数的编码，否则拼接post时，中文用户名将不能正常登录
		HttpEntity loginHttpEntity = new UrlEncodedFormEntity(loginParameters, "UTF-8");
		loginHttpPost.setEntity(loginHttpEntity);
		System.out.println("loginHttpPost.getURI():" + loginHttpPost.getURI());
		loginResult = EntityUtils.toString(httpClient.execute(loginHttpPost).getEntity());
		// 最终结果
		System.out.println("loginResult:" + loginResult);

		// basic
		String basicURL = "http://i.taobao.com/my_taobao.htm";
		HttpGet basicGET = new HttpGet(basicURL);
		Tools.saveToLocal(httpClient.execute(basicGET).getEntity(), "taobao.basic.html");

		// security
		String securityURL = "http://member1.taobao.com/member/fresh/account_security.htm";
		HttpGet securityGET = new HttpGet(securityURL);
		Tools.saveToLocal(httpClient.execute(securityGET).getEntity(), "taobao.security.html");

		// address
		String addressURL = "http://member1.taobao.com/member/fresh/deliver_address.htm";
		HttpGet addressGet = new HttpGet(addressURL);
		Tools.saveToLocal(httpClient.execute(addressGet).getEntity(), "taobao.address.html");

		// itemlist
		String itemURL = "http://trade.taobao.com/trade/itemlist/list_bought_items.htm";
		HttpGet itemGet = new HttpGet(itemURL);
		int i = 1;
		Tools.saveToLocal(httpClient.execute(itemGet).getEntity(), "taobao.item." + i + ".html");
		List<String> itemDetailList = new ArrayList<String>();
		String itemResult = EntityUtils.toString(httpClient.execute(itemGet).getEntity());
		Document itemdoc = Jsoup.parse(itemResult);
		String totalPage = Tools.GetNumber(itemdoc.select("div.total").first().text().trim());

		// 取商品详情链接
		for (Element element : itemdoc.select("a:containsOwn(订单详情)")) {
			String detailUrl = element.attr("href");
			itemDetailList.add(detailUrl);
		}

		if ((totalPage != null) && (totalPage != "") && (Integer.parseInt(totalPage) > 1)) {
			String itemNextUrl = itemdoc.getElementsByClass("num").get(1).attr("href");
			itemNextUrl = itemNextUrl.lastIndexOf("=") != -1 ? itemNextUrl.substring(0, itemNextUrl.lastIndexOf("=")) : null;
			while (i < Integer.parseInt(totalPage)) {
				i++;
				String NextUrl = itemNextUrl + "=" + i;
				System.out.println("NextUrl:" + NextUrl);
				itemGet = new HttpGet(NextUrl);
				Tools.saveToLocal(httpClient.execute(itemGet).getEntity(), "taobao.item." + i + ".html");
				itemdoc = Jsoup.parse(EntityUtils.toString(httpClient.execute(itemGet).getEntity()));
				for (Element element : itemdoc.select("a:containsOwn(订单详情)")) {
					String detailUrl = element.attr("href");
					itemDetailList.add(detailUrl);
				}
			}
		}

		//开始下载详单页面
		int listSize = itemDetailList.size();
		for (int kk = 0; kk < listSize; kk++) {
			System.out.println("url." + kk + ":" + (String) itemDetailList.get(kk));
			HttpGet detailGet = new HttpGet((String) itemDetailList.get(kk));
			Tools.saveToLocal(httpClient.execute(detailGet).getEntity(), "taobao.detail." + kk + ".html");
		}

		long endTime = System.currentTimeMillis();
		System.out.println("EndTime:" + endTime);
		System.out.print("共用时:" + (endTime - StartTime) + "毫秒");
	}
}