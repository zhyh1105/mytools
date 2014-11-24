package com.puhui.crawler;

import java.awt.Desktop;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.puhui.util.HttpUtils;

public class CM_10086 {
    public static void main(String[] args) throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient client = HttpUtils.getHttpClient(true, cookieStore);
        HttpGet get = null;
        HttpPost post = null;
        CloseableHttpResponse response = null;
        String phone = "13552355914";
        String password = "zff829";
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
        System.out.println("是否显示验证码:" + EntityUtils.toString(response.getEntity()));
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
        // cookieStore.addCookie(new BasicClientCookie(" WT_FPC",
        // "id=28be449822f34267fff1416815915636:lv=1416815920482:ss=1416815915636"));
        response = client.execute(HttpUtils.post(url));
        System.out.println(EntityUtils.toString(response.getEntity()));
        response.close();

        // 获取验证码
        url = "https://bj.ac.10086.cn/ac/ValidateNum?smartID=" + System.currentTimeMillis();

        get = HttpUtils.get(url);
        response = client.execute(get);
        File codeFile = new File("D:/tmp/10086/num/code.jpg");
        FileUtils.copyInputStreamToFile(response.getEntity().getContent(), codeFile);
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
        FileUtils.write(loginRusultFile, EntityUtils.toString(response.getEntity(), CHARTSET), CHARTSET);
        response.close();

        Document document = Jsoup.parse(loginRusultFile, CHARTSET);
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
                    params.put(inputs.get(i).attr("name"), inputs.get(i).attr("value"));
                }
                post = HttpUtils.post(url, params);
                response = client.execute(post);
                FileUtils.write(createTempFile(), EntityUtils.toString(response.getEntity(), CHARTSET), CHARTSET);
                response.close();
            }
        }
        printCookies(cookieStore);

        // 获取验证码
        url = "http://www.bj.10086.cn/service/fee/zdcx/";

        get = HttpUtils.get(url);
        response = client.execute(get);
        response.close();
        printCookies(cookieStore);

        client.close();
    }

    private static File createTempFile() {
        return new File("D:/tmp/10086/bills", System.currentTimeMillis() + ".html");
    }

    private static String CHARTSET = "GB2312";

    private static void printCookies(CookieStore cookieStore) {
        for (Cookie cookie : cookieStore.getCookies()) {
            System.out.printf("%s\t%s\n", cookie.getName(), cookie.getValue());
        }
    }
}
