package com.amos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JOptionPane;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.amos.tool.Tools;

/**
 * Created by lixin on 14-6-25.
 */
public class LoginGDMobile {

    public static void main(String args[]) throws Exception {

        String phone="广东移动";
        String password="密码";



        CookieStore cookieStore = new BasicCookieStore();
        //0.创建httpclient
        HttpClient httpClient = Tools.createSSLClientDefaultWithCookie(cookieStore);

        String url = "https://gd.ac.10086.cn/ucs/login/signup.jsps";
        HttpResponse httpResponse = httpClient.execute(new HttpGet(url));

        String result = EntityUtils.toString(httpResponse.getEntity());

        //加密短信验证码
        Matcher matcher = Pattern.compile("var rsa =(.*?);").matcher(result);
        String rsa = null;
        if (matcher.find()) {
            rsa = matcher.group(1);
            Matcher matcher1 = Pattern.compile("n\":\"(.*?)\"").matcher(rsa);
            // {"e":"10001","maxdigits":67,"n":"8e99bd7e58e36f109d0539fdf187091a9f37572ed68f1e9a18cee2d059adfde1d9c036dcde6e57afa90fa8846834490d47aee3da703af4cca03486886473fa2b"};
            if (matcher1.find()) {
                rsa = matcher1.group(1);
            }

        }
        System.out.println("rsa:" + rsa);

        //1.java中执行js方法,返回加密后的密码
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByExtension("js");

        FileReader reader = new FileReader("/home/Created by lixin on /workspace/httpclienttest/httpclient/src/main/java/com/amos/RSA.js");
        scriptEngine.eval(reader);
        Invocable invocable = (Invocable) scriptEngine;
        //1.1加密的密码
        Object encrypt_password = invocable.invokeFunction("encrypt_password", password);

        do {
            //2.获取登录时的图片验证码
            url = "https://gd.ac.10086.cn/ucs/captcha/image/reade.jsps";
            HttpGet httpGet = new HttpGet(url);
            HttpResponse imagCaptchaResponse = httpClient.execute(httpGet);
            Tools.saveToLocal(imagCaptchaResponse.getEntity(), "GDMOBile.imagCaptcha." + System.currentTimeMillis() + ".png");
            System.out.println("请输入图片验证码:");
            String imagCaptcha = new BufferedReader(new InputStreamReader(System.in)).readLine();

            //3.登录网站
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("loginType", "2"));
            params.add(new BasicNameValuePair("mobile", phone));
            params.add(new BasicNameValuePair("password", encrypt_password.toString()));
            params.add(new BasicNameValuePair("imagCaptcha", imagCaptcha));
            params.add(new BasicNameValuePair("cookieMobile", "on"));
            params.add(new BasicNameValuePair("channel", "0"));
            params.add(new BasicNameValuePair("reqType", "0"));
            params.add(new BasicNameValuePair("exp", null));
            params.add(new BasicNameValuePair("cid", "10003"));
            params.add(new BasicNameValuePair("area", "/commodity"));
            params.add(new BasicNameValuePair("resource", "/commodity/servicio/servicioForwarding/query.jsps"));
            params.add(new BasicNameValuePair("backURL", "http://gd.10086.cn/my/myService/myBasicInfo.shtml"));//http://gd.10086.cn/my/REALTIME_LIST_SEARCH.shtml

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);

            url = "https://gd.ac.10086.cn/ucs/login/register.jsps";
            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(entity);
            result = EntityUtils.toString(httpClient.execute(httppost).getEntity());
//            System.out.println("result:" + result);

        } while (result.contains("图形验证码错误，请重新输入"));//密码错误，请重新输入！
        //请改“动态密码”登录或明天再试！
        //{"content":[{"checkName":"com.dzqd.gaf.web.validate.constraint.ImageVerifyCheck","errorCode":"net.sf.oval.constraint.ImageVerify","fieldName":"imagCaptcha","fieldZhName":"","message":"图形验证码错误，请重新输入！"}],"type":"FeildError"}
        //{"attachment":[{"name":"className","value":"com.dzqd.gaf.core.service.ServiceFailException"},{"name":"defaulMessage","value":"发生错误，请稍后再试。"}],"content":{"@type":"com.dzqd.gaf.core.service.ServiceFailException","debugMsg":"ServiceIdentity[LOGINSERVICE]Operation[登录]FailID[crm.crm_im_002.600.100]failMessage[密码错误，请重新输入！]","errorId":"crm.crm_im_002.600.100","errorMsg":"密码错误，请重新输入！","failId":"crm.crm_im_002.600.100","failMessage":"密码错误，请重新输入！","operaType":"LOGIN","serviceIdentity":"LOGINSERVICE","stackTrace":[{"className":"com.dzqd.gaf.core.service.ServiceAssert","fileName":"ServiceAssert.java","lineNumber":80,"methodName":"operationFail","nativeMethod":false},{"className":"com.dzqd.ucs.server.single.signon.login.authentication.password.AbstractPasswrodValidater","fileName":"AbstractPasswrodValidater.java","lineNumber":102,"methodName":"operationFail","nativeMethod":false},{"className":"com.dzqd.ucs.server.single.signon.login.authentication.password.AbstractPasswrodValidater","fileName":"AbstractPasswrodValidater.java","lineNumber":78,"methodName":"validate","nativeMethod":false},{"className":"com.dzqd.ucs.server.single.signon.login.authentication.password.PasswrodValidaterInvocation","fileName":"PasswrodValidaterInvocation.java","lineNumber":57,"methodName":"invok","nativeMethod":false},{"className":"com.dzqd.ucs.server.single.signon.login.authentication.AuthenticationServiceComponent","fileName":"AuthenticationServiceComponent.java","lineNumber":44,"methodName":"authen","nativeMethod":false},{"className":"com.dzqd.ucs.server.single.signon.login.LoginService","fileName":"LoginService.java","lineNumber":142,"methodName":"authenting","nativeMethod":false},{"className":"com.dzqd.ucs.server.single.signon.login.LoginService","fileName":"LoginService.java","lineNumber":88,"methodName":"login","nativeMethod":false},{"className":"com.dzqd.ucs.server.single.signon.login.LoginHandlerController","fileName":"LoginHandlerController.java","lineNumber":282,"methodName":"loginUCS","nativeMethod":false},{"className":"com.dzqd.ucs.server.single.signon.login.LoginHandlerController","fileName":"LoginHandlerController.java","lineNumber":104,"methodName":"register_aroundBody0","nativeMethod":false},{"className":"com.dzqd.ucs.server.single.signon.login.LoginHandlerController$AjcClosure1","fileName":"LoginHandlerController.java","lineNumber":1,"methodName":"run","nativeMethod":false},{"className":"org.aspectj.runtime.reflect.JoinPointImpl","fileName":"JoinPointImpl.java","lineNumber":149,"methodName":"proceed","nativeMethod":false},{"className":"com.dzqd.gaf.web.controller.AbstractControllerIoc","fileName":"AbstractControllerIoc.java","lineNumber":164,"methodName":"around","nativeMethod":false},{"className":"com.dzqd.ucs.server.controller.UCSControllerIoc","fileName":"UCSControllerIoc.java","lineNumber":1,"methodName":"ajc$superDispatch$com_dzqd_ucs_server_controller_UCSControllerIoc$around","nativeMethod":false},{"className":"com.dzqd.ucs.server.controller.UCSControllerIoc","fileName":"UCSControllerIoc.java","lineNumber":29,"methodName":"around","nativeMethod":false},{"className":"com.dzqd.ucs.server.single.signon.login.LoginHandlerController","fileName":"LoginHandlerController.java","lineNumber":89,"methodName":"register","nativeMethod":false},{"className":"sun.reflect.GeneratedMethodAccessor626","lineNumber":-1,"methodName":"invoke","nativeMethod":false},{"className":"sun.reflect.DelegatingMethodAccessorImpl","fileName":"DelegatingMethodAccessorImpl.java","lineNumber":37,"methodName":"invoke","nativeMethod":false},{"className":"java.lang.reflect.Method","fileName":"Method.java","lineNumber":611,"methodName":"invoke","nativeMethod":false},{"className":"org.springframework.web.bind.annotation.support.HandlerMethodInvoker","fileName":"HandlerMethodInvoker.java","lineNumber":421,"methodName":"doInvokeMethod","nativeMethod":false},{"className":"org.springframework.web.bind.annotation.support.HandlerMethodInvoker","fileName":"HandlerMethodInvoker.java","lineNumber":136,"methodName":"invokeHandlerMethod","nativeMethod":false},{"className":"org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter","fileName":"AnnotationMethodHandlerAdapter.java","lineNumber":326,"methodName":"invokeHandlerMethod","nativeMethod":false},{"className":"org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter","fileName":"AnnotationMethodHandlerAdapter.java","lineNumber":313,"methodName":"handle","nativeMethod":false},{"className":"org.springframework.web.servlet.DispatcherServlet","fileName":"DispatcherServlet.java","lineNumber":875,"methodName":"doDispatch","nativeMethod":false},{"className":"org.springframework.web.servlet.DispatcherServlet","fileName":"DispatcherServlet.java","lineNumber":807,"methodName":"doService","nativeMethod":false},{"className":"org.springframework.web.servlet.FrameworkServlet","fileName":"FrameworkServlet.java","lineNumber":571,"methodName":"processRequest","nativeMethod":false},{"className":"org.springframework.web.servlet.FrameworkServlet","fileName":"FrameworkServlet.java","lineNumber":511,"methodName":"doPost","nativeMethod":false},{"className":"javax.servlet.http.HttpServlet","fileName":"HttpServlet.java","lineNumber":738,"methodName":"service","nativeMethod":false},{"className":"javax.servlet.http.HttpServlet","fileName":"HttpServlet.java","lineNumber":831,"methodName":"service","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.servlet.ServletWrapper","fileName":"ServletWrapper.java","lineNumber":1658,"methodName":"service","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.servlet.ServletWrapper","fileName":"ServletWrapper.java","lineNumber":1598,"methodName":"service","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.filter.WebAppFilterChain","fileName":"WebAppFilterChain.java","lineNumber":149,"methodName":"doFilter","nativeMethod":false},{"className":"com.dzqd.gaf.web.filter.MDCFilter","fileName":"MDCFilter.java","lineNumber":33,"methodName":"doFilterInternal","nativeMethod":false},{"className":"org.springframework.web.filter.OncePerRequestFilter","fileName":"OncePerRequestFilter.java","lineNumber":76,"methodName":"doFilter","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.filter.FilterInstanceWrapper","fileName":"FilterInstanceWrapper.java","lineNumber":190,"methodName":"doFilter","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.filter.WebAppFilterChain","fileName":"WebAppFilterChain.java","lineNumber":125,"methodName":"doFilter","nativeMethod":false},{"className":"com.dzqd.ucs.server.security.UcsSecurityRegexFilter","fileName":"UcsSecurityRegexFilter.java","lineNumber":32,"methodName":"doFilterInternal","nativeMethod":false},{"className":"org.springframework.web.filter.OncePerRequestFilter","fileName":"OncePerRequestFilter.java","lineNumber":76,"methodName":"doFilter","nativeMethod":false},{"className":"com.dzqd.ucs.server.controller.UCSFilterProxy","fileName":"UCSFilterProxy.java","lineNumber":52,"methodName":"invokeFilter","nativeMethod":false},{"className":"com.dzqd.ucs.server.controller.UCSFilterProxy","fileName":"UCSFilterProxy.java","lineNumber":33,"methodName":"doFilter","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.filter.FilterInstanceWrapper","fileName":"FilterInstanceWrapper.java","lineNumber":190,"methodName":"doFilter","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.filter.WebAppFilterChain","fileName":"WebAppFilterChain.java","lineNumber":125,"methodName":"doFilter","nativeMethod":false},{"className":"org.springframework.web.filter.CharacterEncodingFilter","fileName":"CharacterEncodingFilter.java","lineNumber":96,"methodName":"doFilterInternal","nativeMethod":false},{"className":"org.springframework.web.filter.OncePerRequestFilter","fileName":"OncePerRequestFilter.java","lineNumber":76,"methodName":"doFilter","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.filter.FilterInstanceWrapper","fileName":"FilterInstanceWrapper.java","lineNumber":190,"methodName":"doFilter","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.filter.WebAppFilterChain","fileName":"WebAppFilterChain.java","lineNumber":125,"methodName":"doFilter","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.filter.WebAppFilterChain","fileName":"WebAppFilterChain.java","lineNumber":80,"methodName":"_doFilter","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.filter.WebAppFilterManager","fileName":"WebAppFilterManager.java","lineNumber":908,"methodName":"doFilter","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.servlet.ServletWrapper","fileName":"ServletWrapper.java","lineNumber":935,"methodName":"handleRequest","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.servlet.ServletWrapper","fileName":"ServletWrapper.java","lineNumber":503,"methodName":"handleRequest","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.servlet.ServletWrapperImpl","fileName":"ServletWrapperImpl.java","lineNumber":181,"methodName":"handleRequest","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.servlet.CacheServletWrapper","fileName":"CacheServletWrapper.java","lineNumber":91,"methodName":"handleRequest","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.WebContainer","fileName":"WebContainer.java","lineNumber":875,"methodName":"handleRequest","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.WSWebContainer","fileName":"WSWebContainer.java","lineNumber":1592,"methodName":"handleRequest","nativeMethod":false},{"className":"com.ibm.ws.webcontainer.channel.WCChannelLink","fileName":"WCChannelLink.java","lineNumber":186,"methodName":"ready","nativeMethod":false},{"className":"com.ibm.ws.http.channel.inbound.impl.HttpInboundLink","fileName":"HttpInboundLink.java","lineNumber":453,"methodName":"handleDiscrimination","nativeMethod":false},{"className":"com.ibm.ws.http.channel.inbound.impl.HttpInboundLink","fileName":"HttpInboundLink.java","lineNumber":515,"methodName":"handleNewRequest","nativeMethod":false},{"className":"com.ibm.ws.http.channel.inbound.impl.HttpInboundLink","fileName":"HttpInboundLink.java","lineNumber":306,"methodName":"processRequest","nativeMethod":false},{"className":"com.ibm.ws.http.channel.inbound.impl.HttpICLReadCallback","fileName":"HttpICLReadCallback.java","lineNumber":83,"methodName":"complete","nativeMethod":false},{"className":"com.ibm.ws.tcp.channel.impl.AioReadCompletionListener","fileName":"AioReadCompletionListener.java","lineNumber":165,"methodName":"futureCompleted","nativeMethod":false},{"className":"com.ibm.io.async.AbstractAsyncFuture","fileName":"AbstractAsyncFuture.java","lineNumber":217,"methodName":"invokeCallback","nativeMethod":false},{"className":"com.ibm.io.async.AsyncChannelFuture","fileName":"AsyncChannelFuture.java","lineNumber":161,"methodName":"fireCompletionActions","nativeMethod":false},{"className":"com.ibm.io.async.AsyncFuture","fileName":"AsyncFuture.java","lineNumber":138,"methodName":"completed","nativeMethod":false},{"className":"com.ibm.io.async.ResultHandler","fileName":"ResultHandler.java","lineNumber":204,"methodName":"complete","nativeMethod":false},{"className":"com.ibm.io.async.ResultHandler","fileName":"ResultHandler.java","lineNumber":775,"methodName":"runEventProcessingLoop","nativeMethod":false},{"className":"com.ibm.io.async.ResultHandler$2","fileName":"ResultHandler.java","lineNumber":905,"methodName":"run","nativeMethod":false},{"className":"com.ibm.ws.util.ThreadPool$Worker","fileName":"ThreadPool.java","lineNumber":1646,"methodName":"run","nativeMethod":false}],"strategy":"NON_STRATEGY"},"type":"SERVICE_FAIL"}
//         cookieStore.addCookie(new BasicClientCookie("CmWebtokenid","13510210849,gd"));

        matcher = Pattern.compile("content\":\"(.*?)\",").matcher(result);
        if (matcher.find()) {
            url = matcher.group(1);
        }


        HttpGet httpGet = new HttpGet(url);
        result = EntityUtils.toString(httpClient.execute(httpGet).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);

        httpGet = new HttpGet(url);
        result = EntityUtils.toString(httpClient.execute(httpGet).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);


        url = "http://gd.10086.cn/include/v2012_cms/newcms/groupVersion.shtml";
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity(), Consts.UTF_8);//groupVersion=2

        url = "http://gd.10086.cn/commodity/servicio/myService/queryIsFirLogin.jsps";//http://gd.10086.cn/commodity/servicio/myService/queryIsFirLogin.jsps
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"));
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity(), Consts.UTF_8);
        //{"content":false,"type":"undefinition"}
        System.out.println("result:" + result);

        url = "http://gd.10086.cn/common/include/public/isOnline.jsp";//是否在线
        result = EntityUtils.toString(httpClient.execute(new HttpPost(url)).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);//
        //{"same":1,"ip":"139.226.36.64","city":"SZ","result":true,"isLogin":0}

        url = "http://gd.10086.cn/commodity/commons/isonline.jsp";//是否在线
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);

        url = "http://gd.ac.10086.cn/ucs/api/cookies/get.jsps?callback=onJSONPServerCallback&date=";
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);

        url = "http://gd.10086.cn/commodity/servicio/myService/queryBrand.jsps";
        httpPost = new HttpPost(url);
        httpPost.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"));
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity(), Consts.UTF_8);
        //{"content":"https://gd.ac.10086.cn/ucs/login/loading.jsps?reqType=0&channel=0&cid=10003&area=%2Fcommodity&resource=%2Fcommodity%2Fservicio%2FmyService%2FqueryBrand.jsps&loginType=2&optional=true&exp=&backURL=http%3A%2F%2Fgd.10086.cn%2Fmy%2FmyService%2FmyBasicInfo.shtml%3Fdt%3D1405699200000","type":"ucs.client.error.notonline"}
        //{"content":2,"type":"undefinition"}
        System.out.println("result:" + result);


        url = "http://gd.10086.cn/commodity/servicio/myService/queryMail.jsps";
        httpPost = new HttpPost(url);
        httpPost.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"));
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity(), Consts.UTF_8);
        //{"attachment":[{"name":"sysCode","value":"000"},{"name":"retCode","value":"000"},{"name":"count","value":1}],"content":"","type":"redirect"}
        System.out.println("result:" + result);

        url = "http://gd.10086.cn/commodity/servicio/track/servicioDcstrack/query.jsps?servCode=MY_BASICINFO";
        httpPost = new HttpPost(url);
        httpPost.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"));
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity(), Consts.UTF_8);
        //{"content":{"COOKIE_USER_BRAND":"EASYOWN","COOKIE_USER_NUM":"18312563244","groupCode":""},"type":"_dcstrack"}
        System.out.println("result:" + result);


        //个人资料
        url = "http://gd.10086.cn/commodity/servicio/servicioForwarding/query.jsps?servCode=MY_BASICINFO&operaType=QUERY";
        httpPost = new HttpPost(url);
        httpPost.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"));
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);//系统繁忙，请稍后再试


        //details

        url = "https://gd.ac.10086.cn/ucs/second/loading.jsps?reqType=0&channel=0&cid=10002&backURL=http%253A%252F%252Fgd.10086.cn%252Fngcrm%252Fhall%252Frevision%252Fpersonal%252FserviceInquire%252FtelExpenseMonthListInquire.jsp&type=2";
        httpGet = new HttpGet(url);
        result = EntityUtils.toString(httpClient.execute(httpGet).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);


        //发送短信验证码
        url = "https://gd.ac.10086.cn/ucs/captcha/dpwd/send.jsps";
        httpPost.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("mobile", phone));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);//提示信息：动态密码已发送，10分钟内有效。
        //动态密码发送失败！ |动态密码在10分钟内只能发送3次。


        //加密smscode
        //加密短信验证码
        url = "https://gd.ac.10086.cn/ucs/second/index.jsps";
        httpPost = new HttpPost(url);
        httpPost.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("type", "2"));
        params.add(new BasicNameValuePair("cid", "10002"));
        params.add(new BasicNameValuePair("channel", "0"));
        params.add(new BasicNameValuePair("reqType", "0"));

        params.add(new BasicNameValuePair("backURL", "http%3A%2F%2Fgd.10086.cn%2Fngcrm%2Fhall%2Frevision%2Fpersonal%2FserviceInquire%2FtelExpenseMonthListInquire.jsp"));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);


        Matcher matchersms = Pattern.compile("var rsa =(.*?);").matcher(result);
        if (matchersms.find()) {
            rsa = matchersms.group(1);
            Matcher matcher1 = Pattern.compile("n\":\"(.*?)\"").matcher(rsa);
            if (matcher1.find()) {
                rsa = matcher1.group(1);
            }
        }
        System.out.println("rsa:" + rsa);

        //java中执行js方法,返回加密后的密码
        scriptEngine = new ScriptEngineManager().getEngineByExtension("js");
        reader = new FileReader("/home/Created by lixin on /workspace/httpclienttest/httpclient/src/main/java/com/amos/RSA.js");
        scriptEngine.eval(reader);
        invocable = (Invocable) scriptEngine;

        do{

        String smsCode = JOptionPane.showInputDialog("请输入短信验证码:");
        //加密的密码
        smsCode = invocable.invokeFunction("encrypt_password", smsCode).toString();

        //验证短信验证码:
        url = "https://gd.ac.10086.cn/ucs/second/authen.jsps";
        httpPost = new HttpPost(url);
        httpPost.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("dpwd", smsCode));
        params.add(new BasicNameValuePair("type", "2"));
        params.add(new BasicNameValuePair("cid", "10002"));
        params.add(new BasicNameValuePair("channel", "0"));
        params.add(new BasicNameValuePair("reqType", "0"));
        params.add(new BasicNameValuePair("backURL", "http%3A%2F%2Fgd.10086.cn%2Fngcrm%2Fhall%2Frevision%2Fpersonal%2FserviceInquire%2FtelExpenseMonthListInquire.jsp"));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);

        }while(result.contains("密码错误，请重新输入！"));

        matcher = Pattern.compile("content\":\"(.*?)\",").matcher(result);
        if (matcher.find()) {
            url = matcher.group(1);
        }
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity(), Consts.UTF_8);
        //{"content":"/ucs/login/loading.jsps?backURL=http%3A%2F%2Fgd.10086.cn%2Fngcrm%2Fhall%2Frevision%2Fpersonal%2FserviceInquire%2FtelExpenseMonthListInquire.jsp&reqType=0&channel=0&cid=10002&exp=","type":"ucs.server.location.url"}
        //http://gd.10086.cn/ngcrm/hall/revision/personal/serviceInquire/telExpenseMonthListInquire.jsp
        System.out.println("result:" + result);


        url="http://gd.10086.cn/v2008/include/public/mainNav.shtml";
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);


        url="http://gd.10086.cn/include/v2012_cms/newcms/groupVersion.shtml";
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);

        url="http://gd.10086.cn/include/v2012_cms/newcms/gdindex201406/gdMainNav_GD.shtml";
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);

        url="http://gd.10086.cn/include/v2012/public/footer.shtml";
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);



        url="http://gd.10086.cn/common/include/public/isOnline.jsp";
        httpPost=new HttpPost(url);
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);

        url = "http://gd.10086.cn/commodity/commons/isonline.jsp";//是否在线
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);


        url="http://gd.ac.10086.cn/ucs/api/cookies/get.jsps?callback=onJSONPServerCallback&date="+new Date().getTime();
        result = EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);


        url="http://gd.10086.cn/ngcrm/hall/service/TelExpenseMonthListInquire.action";
        httpPost=new HttpPost(url);
        httpPost.setHeader(new BasicHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8"));
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("isReRequest","false"));
        params.add(new BasicNameValuePair("_",null));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);
        //http://gd.10086.cn/ngcrm/hall/service/TelExpenseMonthListInquire.action|notrnsmsloginandpwd
//        <th class="tr">查询时间：</th>
//        <td class="tl"><select id="queryMonth" name="queryMonth">
//        <option value="201407">2014年07月</option>
//        <option value="201406">2014年06月</option>
//        <option value="201405">2014年05月</option>
//        <option value="201404">2014年04月</option>

        if(!result.contains("id=\"queryMonth\" name=\"queryMonth")){

            //提交请求
            url="http://gd.10086.cn/ngcrm/hall/ucsLogin";
            httpPost=new HttpPost(url);
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("loginType", null));
            params.add(new BasicNameValuePair("exp", null));
            params.add(new BasicNameValuePair("saTypes", "2"));
            params.add(new BasicNameValuePair("optional", "true"));
            params.add(new BasicNameValuePair("isReRequest", "false"));
            params.add(new BasicNameValuePair("backURL", "http%3A%2F%2Fgd.10086.cn%2Fngcrm%2Fhall%2Frevision%2Fpersonal%2FserviceInquire%2FtelExpenseMonthListInquire.jsp"));
            httpPost.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"));
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            result = EntityUtils.toString(httpClient.execute(httpPost).getEntity(), Consts.UTF_8);
            //{"content":"https://gd.ac.10086.cn/ucs/second/loading.jsps?reqType=0&channel=0&cid=10002&backURL=http%253A%252F%252Fgd.10086.cn%252Fngcrm%252Fhall%252Frevision%252Fpersonal%252FserviceInquire%252FtelExpenseMonthListInquire.jsp&type=2","type":"ucs.client.error.unauthorized"}
            System.out.println("result:" + result);

            //再次查询数据
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("loginType", "2"));
            params.add(new BasicNameValuePair("cookieMobile", "on"));
            params.add(new BasicNameValuePair("channel", "0"));
            params.add(new BasicNameValuePair("reqType", "1"));
            params.add(new BasicNameValuePair("exp", null));
            params.add(new BasicNameValuePair("cid", "10002"));
            params.add(new BasicNameValuePair("area", "/ngcrm"));
            params.add(new BasicNameValuePair("resource", "/ngcrm/hall/ucsLogin"));
            params.add(new BasicNameValuePair("backURL", "http%3A%2F%2Fgd.10086.cn%2Fngcrm%2Fhall%2Frevision%2Fpersonal%2FserviceInquire%2FtelExpenseMonthListInquire.jsp"));
//          params.add(new BasicNameValuePair("backURL", "http://gd.10086.cn/my/myService/myBasicInfo.shtml"));//http://gd.10086.cn/my/REALTIME_LIST_SEARCH.shtml

            url = "https://gd.ac.10086.cn/ucs/login/signup.jsps";
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);

            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(entity);
            result = EntityUtils.toString(httpClient.execute(httppost).getEntity(),Consts.UTF_8);
            System.out.println("result:" + result);


            url="http://gd.10086.cn/ngcrm/hall/service/TelExpenseMonthListInquire.action";
            httpPost=new HttpPost(url);
            httpPost.setHeader(new BasicHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8"));
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("isReRequest","false"));
            params.add(new BasicNameValuePair("_",null));
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            result = EntityUtils.toString(httpClient.execute(httpPost).getEntity(), Consts.UTF_8);
            System.out.println("result:" + result);


        }

        //查询前
        url="http://gd.10086.cn/ngcrm/hall/service/BeforeGotoTelExpenseMonthListInquireResult.action";
        httpPost=new HttpPost(url);
        httpPost.setHeader(new BasicHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8"));
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("queryType","100"));
        params.add(new BasicNameValuePair("queryMonth","201407"));
        params.add(new BasicNameValuePair("realTimeQueryMonthStart","02%2F01%2F2014"));
        params.add(new BasicNameValuePair("realTimeQueryMonthEnd","07%2F20%2F2014"));
        params.add(new BasicNameValuePair("formdate","%E7%82%B9%E5%87%BB%E9%80%89%E6%8B%A9%E6%97%B6%E9%97%B4"));
        params.add(new BasicNameValuePair("enddate","%E7%82%B9%E5%87%BB%E9%80%89%E6%8B%A9%E6%97%B6%E9%97%B4"));
        params.add(new BasicNameValuePair("isReRequest","false"));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);


        //按月查询历史详单.
        url="http://gd.10086.cn/ngcrm/hall/service/TelExpenseMonthListInquireJsonResult.action";
        httpPost=new HttpPost(url);
        httpPost.setHeader(new BasicHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8"));
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("queryType","100"));
        params.add(new BasicNameValuePair("queryMonth","201407"));
        params.add(new BasicNameValuePair("formdate",null));
        params.add(new BasicNameValuePair("enddate",null));
        params.add(new BasicNameValuePair("isReRequest","false"));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        result = EntityUtils.toString(httpClient.execute(httpPost).getEntity(), Consts.UTF_8);
        System.out.println("result:" + result);


    }

}
