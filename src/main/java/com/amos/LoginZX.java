package com.amos;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.nodes.Document;

import com.amos.tool.HttpParser;
import com.amos.tool.Tools;


/**
 * Created by lixin on 14-7-13.
 */
public class LoginZX {

    public static void main(String args[]) throws Exception {
        String user = "lixin180";
        String password = "022215";

        CloseableHttpClient httpClient = Tools.createSSLClientDefault();


        String loginSrcURL = "http://189.cn/dqmh/Uam.do?method=loginUamSendJT&logintype=telephone&shopId=10012&loginRequestURLMark=http://189.cn/dqmh/login/loginJT.jsp";
        HttpGet loginSrcGet = new HttpGet(loginSrcURL);
        HttpContext context = new BasicHttpContext();
        CloseableHttpResponse loginSrcResponse = httpClient.execute(loginSrcGet, context);


        //重定向后如何取URL
        HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
        HttpHost currentHost = (HttpHost) context.getAttribute(
                ExecutionContext.HTTP_TARGET_HOST);
        loginSrcURL = (currentReq.getURI().isAbsolute()) ? currentReq.getURI().toString() : (currentHost.toURI() + currentReq.getURI());


        Document doc = null;
        String result = "";
        String loginSrcHTML = EntityUtils.toString(loginSrcResponse.getEntity());
        do {
            //输入验证码
            Tools.saveToLocal(httpClient.execute(new HttpGet("https://ipcrs.pbccrc.org.cn/imgrc.do?"+System.currentTimeMillis())).getEntity(), "zjtelecom.png");
            String imgCode = JOptionPane.showInputDialog("请输入图片验证码:");

            Tools.println("图片验证码:" + imgCode);
            //form表单中参数

//            forbidpass:null
//            forbidaccounts:null
//            customFileld02:12
//            areaname:浙江
//            username:13336957463
//            customFileld01:3
//            password:022215
//            randomId:0556
//            lt:_c204F306F-F24C-A61E-C0CF-BF92DF3B890A_k1DC10535-43FF-B850-3D92-43B09B997E64
//            _eventId:submit
//            open_no:c2000004
//            authtype:c2000004
//            String[] formParamterData = new String[]{"lt"};

            //取网页中的参数
            List<NameValuePair> loginParametes = new ArrayList<NameValuePair>();

          
            loginParametes.add(new BasicNameValuePair("username", user));
            loginParametes.add(new BasicNameValuePair("password", password));
            loginParametes.add(new BasicNameValuePair("randomId", imgCode));
          

//            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(loginParametes, "UTF-8");

            String lt= HttpParser.getValueFromStartInputByName(loginSrcHTML, "form#c2000004", "lt");
            HttpPost loginPost = new HttpPost(loginSrcURL+"?forbidpass=null&forbidaccounts=null&authtype=c2000004&customFileld02=12&areaname=%E6%B5%99%E6%B1%9F&username=13336957463&customFileld01=3&password=022215&randomId="+imgCode+"&lt="+lt+"&_eventId=submit&open_no=c2000004");
//          loginPost.setEntity(urlEncodedFormEntity);
            CloseableHttpResponse loginPostResponse = httpClient.execute(loginPost);
            int statusCode = loginPostResponse.getStatusLine().getStatusCode();
            //status2
            //验证码输入错误
            System.out.println("statusCode:" + statusCode);
            byte[] bytes = EntityUtils.toByteArray(loginPostResponse.getEntity());
            Tools.saveToLocalByBytes(bytes, "zjtelecom.loginResult.html");
            result = new String(bytes);
            Tools.println(result);
//        } while ((doc.select("#status2")!=null&&doc.select("#status2").first().text().contains("验证码输入错误")));//验证码输入错误

        }
        while (result.contains("验证码不能为空") || result.contains("验证码输入错误") || result.contains("服务器繁忙或网络异常") || result.contains("账号或密码输入错误"));//验证码输入错误

        //服务器繁忙或网络异常，请稍后再试或拨打10000
        //账号或密码输入错误
        //new Regexp("location.replace\\('(.*?)\\)'")
        //Pattern.compile("location.replace\\('(.*?)\\)'")
        System.out.println("success!");
        Matcher matcher = Pattern.compile("location.replace\\('(.*?)'\\)").matcher(result);
        String newURL = "";
        HttpGet httpGet = null;
        HttpResponse loginResponse = null;
        if (matcher.find()) {
            newURL = matcher.group(1);
            httpGet = new HttpGet(newURL);
            loginResponse = httpClient.execute(httpGet);
            result = EntityUtils.toString(loginResponse.getEntity());
            Tools.println(result);


            newURL = "http://189.cn/dqmh/frontLink.do?method=linkTo&shopId=10012&toStUrl=zjpr/bill/getBillDetailInput.htm";
            httpGet = new HttpGet(newURL);
            httpClient.execute(httpGet);
            loginResponse = httpClient.execute(httpGet);
            result = EntityUtils.toString(loginResponse.getEntity());


        }
        matcher = Pattern.compile("location.replace\\('(.*?)'\\)").matcher(result);

        if (matcher.find()) {
            newURL = "http://www.189.cn/zj/service/";
            httpGet = new HttpGet(newURL);
            httpClient.execute(httpGet);
            loginResponse = httpClient.execute(httpGet);
            result = EntityUtils.toString(loginResponse.getEntity());
            Tools.println(result);
        }

    }

}
