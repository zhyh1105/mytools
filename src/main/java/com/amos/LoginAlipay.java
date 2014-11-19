package com.amos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.amos.tool.HttpParser;
import com.amos.tool.Tools;

/**
 * Created by lixin on 14-11-12.
 */
public class LoginAlipay {
    private static HttpParser httpParser = new HttpParser();

    public static void main(String args[]) throws IOException {
        String account = "支付宝账号";
        String password ="支付宝密码";


        String loginURL = "https://auth.alipay.com/login/homeB.htm?redirectType=parent";
        CloseableHttpClient httpClient = Tools.createSSLClientDefault();

        HttpGet httpGet = new HttpGet(loginURL);
        String loginHTML = EntityUtils.toString(httpClient.execute(httpGet).getEntity());

        //验证码url

        Document doc = Jsoup.parse(loginHTML);
        //1).发送图片验证码的链接
        //https://omeo.alipay.com/service/checkcode?sessionID=9895f0a1a59e64a740e2400b413b64b2&t=0.499997429512822
        String imageUrl = doc.select("#J-checkcode-img").attr("src");
        imageUrl=imageUrl.substring(0,imageUrl.lastIndexOf("&"));

        //2).图片验证码的链接
        String verifyResult="";
        String imageCode="";
        do{
            HttpGet imageGet = new HttpGet(imageUrl);
            Tools.saveToLocal(httpClient.execute(imageGet).getEntity(),"alipay.image.png");
            imageCode = JOptionPane.showInputDialog("请输入短信验证码:");
            String verifyImageUrl = "https://auth.alipay.com/login/verifyCheckCode.json?checkCode="+imageCode;
            HttpGet verifyGet = new HttpGet(verifyImageUrl);
            verifyResult = EntityUtils.toString(httpClient.execute(verifyGet).getEntity());
        }
        while(!verifyResult.contains("true"));
        System.out.println("图片验证码识别正常!");

        String passwordSecurityId = httpParser.getValueFromInputByName(loginHTML, "passwordSecurityId");
        String support = httpParser.getValueFromInputByName(loginHTML, "support");
        String needTransfer = httpParser.getValueFromInputByName(loginHTML, "needTransfer");
        String CtrlVersion = httpParser.getValueFromInputByName(loginHTML, "CtrlVersion");
        String loginScene = httpParser.getValueFromInputByName(loginHTML, "loginScene");
        String personalLoginError = httpParser.getValueFromInputByName(loginHTML, "personalLoginError");
        String gotos = httpParser.getValueFromInputByName(loginHTML, "goto");
        String errorGoto = httpParser.getValueFromInputByName(loginHTML, "errorGoto");
        String errorVM = httpParser.getValueFromInputByName(loginHTML, "errorVM");
        String sso_hid = httpParser.getValueFromInputByName(loginHTML, "sso_hid");
        String site = httpParser.getValueFromInputByName(loginHTML, "site");
        String rds_form_token = httpParser.getValueFromInputByName(loginHTML, "rds_form_token");
        String json_tk = httpParser.getValueFromInputByName(loginHTML, "json_tk");
        String method = httpParser.getValueFromInputByName(loginHTML, "method");
        String superSwitch = httpParser.getValueFromInputByName(loginHTML, "superSwitch");
        String noActiveX = httpParser.getValueFromInputByName(loginHTML, "noActiveX");
        String qrCodeSecurityId = httpParser.getValueFromInputByName(loginHTML, "qrCodeSecurityId");
        String J_aliedit_key_hidn = httpParser.getValueFromInputByName(loginHTML, "J_aliedit_key_hidn");
        String J_aliedit_uid_hidn = httpParser.getValueFromInputByName(loginHTML, "J_aliedit_uid_hidn");
        String alieditUid = httpParser.getValueFromInputByName(loginHTML, "alieditUid");
        String REMOTE_PCID_NAME = httpParser.getValueFromInputByName(loginHTML, "REMOTE_PCID_NAME");
        String _seaside_gogo_pcid = httpParser.getValueFromInputByName(loginHTML, "_seaside_gogo_pcid");
        String _seaside_gogo_ = httpParser.getValueFromInputByName(loginHTML, "_seaside_gogo_");
        String _seaside_gogo_p = httpParser.getValueFromInputByName(loginHTML, "_seaside_gogo_p");
        String J_aliedit_prod_type = httpParser.getValueFromInputByName(loginHTML, "J_aliedit_prod_type");
        String idPrefix = httpParser.getValueFromInputByName(loginHTML, "idPrefix");
        List<NameValuePair> parameterList = new ArrayList<NameValuePair>();
        parameterList.add(new BasicNameValuePair("passwordSecurityId",passwordSecurityId));
        parameterList.add(new BasicNameValuePair("logonId",account));
        parameterList.add(new BasicNameValuePair("password",password));
        parameterList.add(new BasicNameValuePair("support",support));
        parameterList.add(new BasicNameValuePair("needTransfer",needTransfer));
        parameterList.add(new BasicNameValuePair("CtrlVersion",CtrlVersion));
        parameterList.add(new BasicNameValuePair("loginScene",loginScene));
        parameterList.add(new BasicNameValuePair("personalLoginError",personalLoginError));
        parameterList.add(new BasicNameValuePair("goto",gotos));
        parameterList.add(new BasicNameValuePair("errorGoto",errorGoto));
        parameterList.add(new BasicNameValuePair("errorVM",errorVM));
        parameterList.add(new BasicNameValuePair("errorVM",errorVM));
        parameterList.add(new BasicNameValuePair("site",site));
        parameterList.add(new BasicNameValuePair("rds_form_token",rds_form_token));
        parameterList.add(new BasicNameValuePair("json_tk",json_tk));
        parameterList.add(new BasicNameValuePair("method",method));
        parameterList.add(new BasicNameValuePair("superSwitch",superSwitch));
        parameterList.add(new BasicNameValuePair("noActiveX",noActiveX));
        parameterList.add(new BasicNameValuePair("qrCodeSecurityId",qrCodeSecurityId));
        parameterList.add(new BasicNameValuePair("J_aliedit_key_hidn",J_aliedit_key_hidn));
        parameterList.add(new BasicNameValuePair("J_aliedit_uid_hidn",J_aliedit_uid_hidn));
        parameterList.add(new BasicNameValuePair("alieditUid",alieditUid));
        parameterList.add(new BasicNameValuePair("REMOTE_PCID_NAME",REMOTE_PCID_NAME));
        parameterList.add(new BasicNameValuePair("_seaside_gogo_pcid",_seaside_gogo_pcid));
        parameterList.add(new BasicNameValuePair("_seaside_gogo_",_seaside_gogo_));
        parameterList.add(new BasicNameValuePair("_seaside_gogo_p",_seaside_gogo_p));
        parameterList.add(new BasicNameValuePair("J_aliedit_prod_type",J_aliedit_prod_type));
        parameterList.add(new BasicNameValuePair("idPrefix",idPrefix));
        parameterList.add(new BasicNameValuePair("checkCode",imageCode));

        HttpPost loginPost = new HttpPost(loginURL);
        loginPost.setEntity(new UrlEncodedFormEntity(parameterList));
        CloseableHttpResponse loginResonse = httpClient.execute(loginPost);
        String loginPostResult = EntityUtils.toString(loginResonse.getEntity());
        System.out.println(loginPostResult);

        //交易
        String historyBuyURL = "https://consumeprod.alipay.com/record/standard.htm?beginDate=2013.07.13&endDate=2014.07.12&dateRange=customDate";
        HttpGet historyBuyGET = new HttpGet(historyBuyURL);
        Tools.saveToLocal(httpClient.execute(historyBuyGET).getEntity(),"apliay.trade.html");;

        String oneYearAgoBuyURL = "https://lab.alipay.com/consume/record/historyIndexNew.htm";
        HttpGet oneYearAgoBuyGET = new HttpGet(oneYearAgoBuyURL);
        Tools.saveToLocal(httpClient.execute(oneYearAgoBuyGET).getEntity(),"apliay.trade.oneyear.ago.html");;

        //J-qrcode-errorBox
//        support:000001
//        needTransfer:
//        CtrlVersion:1,1,0,1
//        loginScene:home
//        redirectType:parent
//        personalLoginError:
//        goto:
//        errorGoto:
//        errorVM:scene/homeB.vm
//        sso_hid:
//        site:
//        rds_form_token:LnkybHZrzZ5WaAL1qnrhulGmbMCXLi8T
//        json_tk:
//        method:
//        logonId:18962117738
//        superSwitch:true
//        noActiveX:false

//        passwordSecurityId:web|authcenter_querypwd_login|39b954e2-81ec-4e13-85ad-202339ce8eee
//        qrCodeSecurityId:web|authcenter_qrcode_login|3726c0d9-10ba-4db8-a368-4f9658f1cc09
//        password_input:990099
//        J_aliedit_using:false
//        password:990099
//        J_aliedit_key_hidn:password
//        J_aliedit_uid_hidn:alieditUid
//        alieditUid:dc749bd25ece50cc3f9ab1c12ae181b6
//        REMOTE_PCID_NAME:_seaside_gogo_pcid
//        _seaside_gogo_pcid:
//        _seaside_gogo_:
//        _seaside_gogo_p:
//        J_aliedit_prod_type:
//        checkCode:dbxw
//        idPrefix:




    }




}
