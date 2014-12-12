package com.amos;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringEscapeUtils;
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
public class LoginZJMobile {


    public static void main(String args[]) throws Exception {
        String phone = "18730639255";
        String password = "829829";

        HttpClient httpClient = Tools.createSSLClientDefault();
        HttpPost httpPost = null;
        List<NameValuePair> params = new ArrayList<NameValuePair>();
//        String url = "https://he.ac.10086.cn/login";//sc
        String url = "https://he.ac.10086.cn/login";//sc


        HttpGet httpGet = new HttpGet(url);
        HttpResponse loginResponse = httpClient.execute(httpGet);
        String result = EntityUtils.toString(loginResponse.getEntity());

        do {
            url = "https://he.ac.10086.cn/ImgDisp";
            Tools.saveToLocal(httpClient.execute(new HttpGet(url)).getEntity(), "heimg.png");
            String imgCode = JOptionPane.showInputDialog("输入图片验证码");

            url = "https://he.ac.10086.cn/loginbox";
            params.add(new BasicNameValuePair("service", "my"));
            params.add(new BasicNameValuePair("continue", "%2Fmy%2Flogin%2FloginSuccess.do"));
            params.add(new BasicNameValuePair("failurl", "https%3A%2F%2Fhe.ac.10086.cn%2Flogin"));
            params.add(new BasicNameValuePair("style", "1"));
            params.add(new BasicNameValuePair("pwdType", "2"));
            params.add(new BasicNameValuePair("SMSpwdType", "0"));
            params.add(new BasicNameValuePair("billId", phone));
            params.add(new BasicNameValuePair("passwd1", "%CD%FC%BC%C7%C3%DC%C2%EB%A3%BF%BF%C9%D3%C3%B6%AF%CC%AC%C3%DC%C2%EB%B5%C7%C2%BC"));
            params.add(new BasicNameValuePair("passwd", password));
            params.add(new BasicNameValuePair("validCode", imgCode));

            httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            result = EntityUtils.toString(httpClient.execute(httpPost).getEntity());
        }while(result.contains("验证码不正确"));

        //<script language="javascript">window.history.forward();</script><form name="authnresponseform" method="post" action="http://www.he.10086.cn/my/sso"><input type="hidden" name="SAMLart" value="6c5bc8cb27a143fba3207a6fa2d5afad"/><input type="hidden" name="RelayState" value="%2Fmy%2Flogin%2FloginSuccess.do"/><input type="submit" name="submit" style="display:none"></form><script>document.authnresponseform.submit.click()</script>
        Matcher matcher = Pattern.compile("action=\"(.*?)\"").matcher(result);
        if(matcher.find()){
            url=matcher.group(1);
            System.out.println("url:"+url);
        }
        matcher = Pattern.compile("name=\"SAMLart\" value=\"(.*?)\"").matcher(result);

        String SAMLart = "";
        if(matcher.find()){
            SAMLart=matcher.group(1);
            System.out.println("SAMLart:"+SAMLart);
        }

        matcher = Pattern.compile("name=\"RelayState\" value=\"(.*?)\"").matcher(result);
        String RelayState = "";
        if(matcher.find()){
            RelayState=matcher.group(1);
            System.out.println("RelayState:"+RelayState);
        }

        params.clear();
        params.add(new BasicNameValuePair("SAMLart", SAMLart));
        params.add(new BasicNameValuePair("RelayState",RelayState));
        httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type","application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity()) ;
        //<script language='javascript'>window.history.forward();</script><form name='authnrequestform' method='post' action='http://www.he.10086.cn/my/UnifiedLoginClientServlet'><input type='hidden' name='RelayState' value='%2Fmy%2Flogin%2FloginSuccess.do'/><input type='hidden' name='SAMLart' value='0c49f53bae13418aa6881aa75b13ef53'/><input type='hidden' name='jumpUrl' value='%2Fmy%2Flogin%2FloginSuccess.do'/><input type='hidden' name='loginUrl' value='http%3A%2F%2Fwww.he.10086.cn%2Fmy%2Flogin%2Flogin.jsp'/><input type='submit' name='submit' style='display:none'/></form><script>document.authnrequestform.submit.click()</script>

        matcher=Pattern.compile("action=[\'|\"](.*?)[\'|\"]").matcher(result);
        if(matcher.find()){
            url=matcher.group(1);
            System.out.println("url:"+url);
        }


        String jumpUrl = "";
        matcher=Pattern.compile("name=['|\"]jumpUrl['|\"] value=[\'|\"](.*?)[\'|\"]").matcher(result);
        if(matcher.find()){
            jumpUrl=matcher.group(1);
            System.out.println("jumpUrl:"+jumpUrl);
        }

        String loginUrl = "";
        matcher=Pattern.compile("name=['|\"]loginUrl['|\"] value=[\'|\"](.*?)[\'|\"]").matcher(result);
        if(matcher.find()){
            loginUrl=matcher.group(1);
            System.out.println("loginUrl:"+loginUrl);
        }

        SAMLart = "";
        matcher=Pattern.compile("name=['|\"]SAMLart['|\"] value=[\'|\"](.*?)[\'|\"]").matcher(result);
        if(matcher.find()){
            SAMLart=matcher.group(1);
            System.out.println("SAMLart:"+SAMLart);
        }

        matcher=Pattern.compile("name=['|\"]RelayState['|\"] value=[\'|\"](.*?)[\'|\"]").matcher(result);
        if(matcher.find()){
            RelayState=matcher.group(1);
            System.out.println("RelayState:"+RelayState);
        }

        params.clear();
        params.add(new BasicNameValuePair("jumpUrl",jumpUrl));
        params.add(new BasicNameValuePair("RelayState",RelayState));
        params.add(new BasicNameValuePair("loginUrl",loginUrl));
        params.add(new BasicNameValuePair("SAMLart",SAMLart));
        params.add(new BasicNameValuePair("submit","Submit"));

        httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type","application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity()) ;
//        <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
//        <html xmlns="http://www.w3.org/1999/xhtml">
//        <body
//                onLoad="window.parent.parent.location='/my/index.jsp'">
//        </body>
//        </html>

        if(result.contains("my/index.jsp")){
            url="http://www.he.10086.cn/my/index.jsp";
        }
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity()) ;
        if(result.contains("/my/index.do")){
            url="http://www.he.10086.cn/my/index.do";
        }
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity()) ;

        url="http://www.he.10086.cn/my/queryAccountAndCostInfo.do?AISSO_LOGIN=true";
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity()) ;


        url = "http://www.he.10086.cn/my/queryPresentFlow.do?AISSO_LOGIN=true";
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity()) ;

        //账单查询
        url="http://www.he.10086.cn/my/include/mybill.jsp";
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity()) ;

//        <a href="http://service.he.10086.cn/yw/bill/billDetailBefore.do?bid=BD399F39E69248CFE044001635842131" target="_blank" title="历史月账单查询"
//        <a href="http://service.he.10086.cn/yw/bill/billDetail.do?bid=BD399F39E69148CFE044001635842132&amp;month=0" target="_blank" title="实时账单查询"
//        <a href="http://service.he.10086.cn/yw/bill/billDetailBefore.do?bid=BD399F39E69248CFE044001635842131" target="_blank" title="历史月账单查询"

        matcher = Pattern.compile("href=\"(.*?)\"\\s+target=\"_blank\"\\s+title=\"实时账单查询\"").matcher(result);
        if(matcher.find()){
            url=StringEscapeUtils.unescapeHtml4(matcher.group(1));
        }
        result = EntityUtils.toString(httpClient.execute(new HttpPost(url)).getEntity()) ;

        matcher = Pattern.compile("href=\"(.*?)\"\\s+target=\"_blank\"\\s+title=\"历史月账单查询\"").matcher(result);
        if(matcher.find()){
            url=StringEscapeUtils.unescapeHtml4(matcher.group(1)).replace("billDetailBefore","billDetail");
        }
        params.clear();
        params.add(new BasicNameValuePair("month","03=2014"));
        params.add(new BasicNameValuePair("listtype",""));
        params.add(new BasicNameValuePair("validateCode",""));
        httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity()) ;


//        matcher = Pattern.compile("<form action=\"(.*?)\"").matcher(result);
//        if(matcher.find()){
//            url="http://service.he.10086.cn"+StringEscapeUtils.unescapeHtml4(matcher.group(1));
//        }


        //[基本信息],要发短信才能查看
        url="http://www.he.10086.cn/my/userinfo/userYdinfoFirst.jsp";
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity()) ;

        //发送短信验证码
        url="http://www.he.10086.cn/my/userinfo/sendMessage.do";
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity()) ;
        //{ERROR_CODE:'0',ERROR_MSG:'成功',ERROR_HINT:'成功',SNS_CODE:'null'}

        System.out.print("result:"+result);
        httpGet.releaseConnection();

    }
}
