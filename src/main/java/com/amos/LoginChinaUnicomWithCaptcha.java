package com.amos;


import javax.swing.JOptionPane;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.amos.tool.Tools;

/**
 * 
 */
public class LoginChinaUnicomWithCaptcha {

    public static void main(String args[]) throws Exception {

        String name = "中国联通";
        String pwd = "密码";

        //生成验证码的链接
        String createCaptchaUrl = "http://uac.10010.com/portal/Service/CreateImage";
        HttpClient httpClient = new DefaultHttpClient();

        //这里可自定义所需要的cookie
        CookieStore cookieStore = new BasicCookieStore();

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();

        //get captcha,获取验证码
        HttpGet captchaHttpGet = new HttpGet(createCaptchaUrl);
        HttpResponse capthcaResponse = httpClient.execute(captchaHttpGet);

        if (capthcaResponse.getStatusLine().getStatusCode() == 200) {
            //将验证码写入本地
            Tools.saveToLocal(capthcaResponse.getEntity(), "chinaunicom.capthca." + System.currentTimeMillis()+".png");
        }


        //手工输入验证码并验证
        HttpResponse verifyResponse = null;
        String capthca = null;
        String uvc = null;

        do {
            //输入验证码,读入键盘输入
            //1)
//            InputStream inputStream = System.in;
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//            System.out.println("请输入验证码:");
//            capthca = bufferedReader.readLine();

            capthca=JOptionPane.showInputDialog("请输入图片验证码:");

            //2)
            //Scanner scanner = new Scanner(System.in);
            //capthca = scanner.next();
           // http://uac.10010.com/portal/Service/CtaIdyChk?callback=jsonp1404716227598&verifyCode=4m3e&verifyType=1
            String verifyCaptchaUrl = "http://uac.10010.com/portal/Service/CtaIdyChk?verifyCode=" + capthca + "&verifyType=1";
            HttpGet verifyCapthcaGet = new HttpGet(verifyCaptchaUrl);
            verifyResponse = httpClient.execute(verifyCapthcaGet);
            AbstractHttpClient abstractHttpClient = (AbstractHttpClient) httpClient;
            for (Cookie cookie : abstractHttpClient.getCookieStore().getCookies()) {
                System.out.println(cookie.getName() + ":" + cookie.getValue());
                if (cookie.getName().equals("uacverifykey")) {
                    uvc = cookie.getValue();
                }
            }
        } while (!EntityUtils.toString(verifyResponse.getEntity()).contains("true"));

        //登录
        String loginurl = "https://uac.10010.com/portal/Service/MallLogin?userName=" + name + "&password=" + pwd + "&pwdType=01&productType=01&verifyCode=" + capthca + "&redirectType=03&uvc=" + uvc;
        HttpGet loginGet = new HttpGet(loginurl);
        CloseableHttpResponse loginResponse = httpclient.execute(loginGet);
        System.out.print("result:" + EntityUtils.toString(loginResponse.getEntity()));

        //抓取基本信息数据
        //jsonp1404663560635({resultCode:"7072",redirectURL:"http://www.10010.com",errDesc:"null",msg:'系统忙，请稍后再试。',needvode:"1"});
        HttpPost basicHttpGet = new HttpPost("http://iservice.10010.com/ehallService/static/acctBalance/execute/YH102010005/QUERY_AcctBalance.processData/Result");
        Tools.saveToLocal(httpclient.execute(basicHttpGet).getEntity(), "chinaunicom.basic.html");

    }

}
