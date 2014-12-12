package com.amos;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.amos.tool.Tools;

/**
 * @author Created by lixin on 
 *         登录并抓取中国联通数据
 */

public class LoginChinaUnicom {
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

    	String name = "18500356045";
		String pwd = "2333";

        //https://uac.10010.com/portal/Service/MallLogin?callback=jQuery17208151653951499611_1404661522215&redirectURL=http%3A%2F%2Fwww.10010.com&userName=13167081006&password=0077450&pwdType=01&productType=01&redirectType=01&rememberMe=1&_=1404661572740
        String url = "https://uac.10010.com/portal/Service/MallLogin?callback=jQuery17202691898950318097_1403425938090&redirectURL=http%3A%2F%2Fwww.10010.com&userName=" + name + "&password=" + pwd + "&pwdType=01&productType=01&redirectType=01&rememberMe=1";
        HttpEntity httpEntity;
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        //初始化结果
        String result = "";
        byte[] bytes = null;
        HttpResponse loginResponse = httpClient.execute(httpGet);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (loginResponse.getStatusLine().getStatusCode() == 200) {
            for (Header head : loginResponse.getAllHeaders()) {
                System.out.println(head);
            }
            HttpEntity loginEntity = loginResponse.getEntity();
            String loginEntityContent = EntityUtils.toString(loginEntity);
            System.out.println("登录状态:" + loginEntityContent);
          
            //jQuery17208151653951499611_1404661522215({resultCode:"7007",redirectURL:"http://www.10010.com",errDesc:"null",msg:'用户名或密码不正确。<a href="https://uac.10010.com/cust/resetpwd/inputName" target="_blank" style="color: #36c;cursor: pointer;text-decoration:underline;">忘记密码？</a>',needvode:"1"});
            //如果登录成功
            if (loginEntityContent.contains("resultCode:\"0000\"")) {

                //月份
            	String months[] = new String[]{"201409", "201408","201410", "201407"};

              /*  for (String month : months) {
                    //http://iservice.10010.com/ehallService/static/historyBiil/execute/YH102010002/QUERY_YH102010002.processData/QueryYH102010002_Data/201405/undefined?_=1404661790076&menuid=000100020001
                   // String billurl = "http://iservice.10010.com/ehallService/static/historyBiil/execute/YH102010002/QUERY_YH102010002.processData/QueryYH102010002_Data/" + month + "/undefined";
                	
                	String billurl ="http://iservice.10010.com/ehallService/static/queryMonth/execute2/YHgetMonths/QUERY_paramSession.processData/QUERY_paramSession_Data/000100030001/201411/undefined/undefined/undefined?_=1415870428148&menuid=000100030001"  ;
                    HttpPost httpPost = new HttpPost(billurl);
                    HttpResponse billresponse = httpClient.execute(httpPost);
                    if (billresponse.getStatusLine().getStatusCode() == 200) {
                        Tools.saveToLocal(billresponse.getEntity(), "chinaunicom.bill." + month + ".4.html");
                    }
                }*/
            	//通话详单
            	//String params = "/03"+"/201410/"+"20141001/"+"20141031"+"/"+querytypeGW;

            	//String billurl ="http://iservice.10010.com/ehallService/static/queryMonth/execute2/YHgetMonths/QUERY_paramSession.processData/QUERY_paramSession_Data/"+;
            	//String billurl ="http://iservice.10010.com/ehallService/static/queryMonth/execute2/YHgetMonths/QUERY_paramSession.processData/QUERY_paramSession_Data/000100030001/201410/20141001/20141031/undefined";
            
            	String billurl ="http://iservice.10010.com/ehallService/static/queryMonth/execute2/YHgetMonths/QUERY_paramSession.processData/QUERY_paramSession_Data/000100030001/";
            	
            	HttpPost httpPost = new HttpPost(billurl);
            	httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
              
                //httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
                httpPost.addHeader("X-Requested-With","XMLHttpRequest");
            	
            	params.add(new BasicNameValuePair("yuefen", "201410"));
                params.add(new BasicNameValuePair("startDate", "2014-10-01"));
                params.add(new BasicNameValuePair("endDate", "2014-10-31"));
                
                httpPost.setEntity(new UrlEncodedFormEntity(params));
                
                bytes = EntityUtils.toByteArray(httpClient.execute(httpPost).getEntity());
                result = new String(bytes);
                System.out.println("result:"+result);

                Tools.saveToLocalByBytes(bytes,"lixin.billlist.html");
                
                HttpResponse billresponse = httpClient.execute(httpPost);
                
                
                if (billresponse.getStatusLine().getStatusCode() == 200) {
                    Tools.saveToLocal(billresponse.getEntity(), "chinaunicom.bill." + "month2" + ".4.html");
                }
            }
        }

    }





}
