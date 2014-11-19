package com.amos.Pay;


import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.amos.tool.Tools;

/**
 * Created by lixin on 14-7-31.
 */
public class UPAY {

    public static void main(String args[]) throws Exception {

        String phone = "kkk";
        String passCode = "9804053365833888635".replaceAll("\\s", "");


        CloseableHttpClient client = Tools.createSSLClientDefault();
        String url = "https://upay.10010.com/npfweb/npfcellweb/phone_recharge_fill.htm";
        String result = EntityUtils.toString(client.execute(new HttpGet(url)).getEntity());
        Matcher matcher = Pattern.compile("name=\"secstate.state\" type=\"hidden\" value=\"(.*?)\"").matcher(result);
        String secstate = "3mCBuETgA/YTbuZO79gHFA==^@^0.0.1";
        if (matcher.find()) {
            secstate = matcher.group(1);
        }


        url = MessageFormat.format("https://upay.10010.com/npfweb/getArea/init?callback=&phoneNo={0}", phone);
        result = EntityUtils.toString(client.execute(new HttpGet(url)).getEntity(), Consts.UTF_8);
//        返回值
//        ({'prov' : '038X389X福建X三明'})
//        ({'prov' : '11X110'})
//        ({'prov' : '031X310X上海X上海'})

        matcher = Pattern.compile("X").matcher(result);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        if (count != 3) {
            System.out.println("手机格式有问题!");
            return;
        }

        String provinceCode = "";
        String cityCode = "";

        matcher = Pattern.compile("\'prov\' : \'(.*?)X(.*?)X(.*?)X(.*?)\'").matcher(result);
        if (matcher.find()) {
            provinceCode = matcher.group(1);
            cityCode = matcher.group(2);

            System.out.println("provinceCode:" + provinceCode);
            System.out.println("cityCode:" + cityCode);
            System.out.println("province:" + matcher.group(3));
            System.out.println("city:" + matcher.group(4));
        }

        url = "https://upay.10010.com/npfweb/NpfCellWeb/reCharge/reChargeCheck";
        String param = String.format("secstate.state=%s&commonBean.userChooseMode=0&commonBean.phoneNo=%s&commonBean.provinceCode=%s&commonBean.cityCode=%s&rechargeMode=%s&cardBean.cardValueCode=04&offerPriceStrHidden=98.50&cardBean.cardValue=100&cardBean.minCardNum=1&cardBean.maxCardNum=3&MaxThreshold01=150&MinThreshold01=1&MaxThreshold02=10&MinThreshold02=1&MaxThreshold03=6&MinThreshold03=1&MaxThreshold04=3&MinThreshold04=1&MaxThreshold05=1&MinThreshold05=1&actionPayFeeInfo.actionConfigId=&commonBean.payAmount=98.50&invoiceBean.need_invoice=1&invoiceBean.invoice_type=&invoiceBean.is_mailing=1&saveflag=false&invoiceBean.post_list=&invoiceBean.invoice_list=&rechargeBean.cardPwd=%s&commonBean.bussineType="
                , secstate, phone, provinceCode, cityCode, "用充值卡", passCode);

        url = url + "?" + param;
        URL encodeURL = new URL(url);
        URI uri = new URI(encodeURL.getProtocol(), encodeURL.getUserInfo(), encodeURL.getHost(), encodeURL.getPort(), encodeURL.getPath(), encodeURL.getQuery(), "");

        result = EntityUtils.toString(client.execute(new HttpGet(uri)).getEntity(), Consts.UTF_8);
        //{"secstate":"3mCBuETgA/YTbuZO79gHFA==^@^0.0.1","out":"尊敬的用户，您好！为了更好的为您提供服务，我公司计划于2014年07月31日22：20至2014年08月01日06：30对系统进行升级。由此给您带来的不便，敬请谅解，感谢您对我们的一贯支持！"}
        //{"secstate":"3mCBuETgA/YTbuZO79gHFA==^@^0.0.1","out":"请正确输入充值卡密码。"}
        //{"secstate":"3mCBuETgA/YTbuZO79gHFA==^@^0.0.1","out":"请正确输入手机号码。"}
        //{"secstate":"3mCBuETgA/YTbuZO79gHFA==^@^0.0.1","out":"尊敬的用户您好，您输入的号码无法使用该业务，详情请咨询10010。"}
        //{"secstate":"3mCBuETgA/YTbuZO79gHFA==^@^0.0.1","out":"尊敬的用户您好，目前系统繁忙，请稍后再试，由此给您造成的不便，敬请谅解。"}
        //{"secstate":"3mCBuETgA/YTbuZO79gHFA==^@^0.0.1","out":"充值卡已使用完"}
        //{"secstate":"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\ncRLkhsgulQ==^@^0.0.1","out":"success"}
        System.out.print("result:" + result);
        //https://upay.10010.com/npfweb/NpfCellWeb/reCharge/reChargeCheck?secstate.state=3mCBuETgA%2FYTbuZO79gHFA%3D%3D%5E%40%5E0.0.1&commonBean.userChooseMode=0&commonBean.phoneNo=13167081006&commonBean.provinceCode=031&commonBean.cityCode=310&rechargeMode=%E7%94%A8%E5%85%85%E5%80%BC%E5%8D%A1&cardBean.cardValueCode=04&offerPriceStrHidden=98.50&cardBean.cardValue=100&cardBean.minCardNum=1&cardBean.maxCardNum=3&MaxThreshold01=150&MinThreshold01=1&MaxThreshold02=10&MinThreshold02=1&MaxThreshold03=6&MinThreshold03=1&MaxThreshold04=3&MinThreshold04=1&MaxThreshold05=1&MinThreshold05=1&actionPayFeeInfo.actionConfigId=&commonBean.payAmount=98.50&invoiceBean.need_invoice=1&invoiceBean.invoice_type=&invoiceBean.is_mailing=1&saveflag=false&invoiceBean.post_list=&invoiceBean.invoice_list=&rechargeBean.cardPwd=9804053365833888635&commonBean.bussineType=

        if (result.contains("success")) {
            System.out.print("卡号正确!");
            System.out.print("result:" + result);

            matcher = Pattern.compile("secstate\":\"(.*?)\"").matcher(result);
            if (matcher.find()) {
                secstate = matcher.group(1);//secstate = URLEncoder.encode(secstate) ;
                System.out.println(secstate);
                secstate = secstate.replaceAll("\\\\n", "%0D%0A");
                System.out.println("secstate:"+secstate);
                System.out.println("URLEncoder.encode(secstate):"+URLEncoder.encode(secstate));
                System.out.println("URLEncoder.encode(secstate):"+URLEncoder.encode(secstate));
                System.out.println(URLEncoder.encode(secstate, "UTF-8"));
            }

            List<NameValuePair> postParams = new ArrayList<NameValuePair>();
            postParams.add(new BasicNameValuePair("secstate.state", secstate));
//            postParams.add(new BasicNameValuePair("commonBean.userChooseMode", "0"));
//            postParams.add(new BasicNameValuePair("commonBean.phoneNo", phone));
//            postParams.add(new BasicNameValuePair("commonBean.provinceCode", provinceCode));
//            postParams.add(new BasicNameValuePair("commonBean.cityCode", cityCode));
//            postParams.add(new BasicNameValuePair("rechargeMode", "%E7%94%A8%E5%85%85%E5%80%BC%E5%8D%A1"));
////            postParams.add(new BasicNameValuePair("rechargeMode","用充值卡"));
//            postParams.add(new BasicNameValuePair("cardBean.cardValueCode", "04"));
//            postParams.add(new BasicNameValuePair("offerPriceStrHidden", "98.50"));
//            postParams.add(new BasicNameValuePair("cardBean.cardValue", "100"));
//            postParams.add(new BasicNameValuePair("cardBean.minCardNum", "1"));
//            postParams.add(new BasicNameValuePair("cardBean.maxCardNum", "3"));
//            postParams.add(new BasicNameValuePair("MaxThreshold01", "150"));
//            postParams.add(new BasicNameValuePair("MinThreshold01", "1"));
//
//            postParams.add(new BasicNameValuePair("MaxThreshold02", "10"));
//            postParams.add(new BasicNameValuePair("MinThreshold02", "1"));
//
//            postParams.add(new BasicNameValuePair("MaxThreshold03", "6"));
//            postParams.add(new BasicNameValuePair("MinThreshold03", "1"));
//
//            postParams.add(new BasicNameValuePair("MaxThreshold04", "3"));
//            postParams.add(new BasicNameValuePair("MinThreshold04", "1"));
//
//            postParams.add(new BasicNameValuePair("MaxThreshold05", "1"));
//            postParams.add(new BasicNameValuePair("MinThreshold05", "1"));
//
//            postParams.add(new BasicNameValuePair("actionPayFeeInfo.actionConfigId", null));
//            postParams.add(new BasicNameValuePair("commonBean.payAmount", "98.50"));
//
//            postParams.add(new BasicNameValuePair("invoiceBean.need_invoice", "1"));
//            postParams.add(new BasicNameValuePair("invoiceBean.invoice_type", null));
//
//            postParams.add(new BasicNameValuePair("invoiceBean.is_mailing", "1"));
//            postParams.add(new BasicNameValuePair("saveflag", "false"));
//            postParams.add(new BasicNameValuePair("invoiceBean.post_list", null));
//            postParams.add(new BasicNameValuePair("invoiceBean.invoice_list", null));
//
//            postParams.add(new BasicNameValuePair("rechargeBean.cardPwd", passCode));
//            postParams.add(new BasicNameValuePair("commonBean.bussineType", null));

//            secstate.state:FYlSiTsg5fqfr%2BwYGrqIRld1TFrxqVZG2Tk04fMFJJPvGafhy7tlRuRUnVqakSpkd7OLifKt7OOZ%0D%0AxN0mv5ywHlg84ClEuATAVr4clSPTODqjNrAOk4F%2FUhdU7beB9G%2FaCPnO5anxinfv5Cu%2F1jgky3H1%0D%0ArD6nYoiUrHwSEdJku56kFoEMrD50WZPiCcX0PUbPPF23ottCCNznmu%2BVLNHVDTwW0YkfwhmASEFV%0D%0AmfGCkySHWj3pmvRWaqMLmbKDeMymWJxjThWl%2FnY%2FaJR5KnoYzwL4MinkT8X814IFHEkbS2bYEE1n%0D%0AleBODrv3kKewHvkn6RjFhyCg97artxYaEnkB4jYFBqhuBBDm2xft6vqBDH%2FZBsmapUyL4ucCNIDw%0D%0AsCa5oFg9NSdyxfaG7OfCitFFWuRKckqfOa2T2wLpR5TQUk8fGZLw21BAalVCmztIqyf%2B2pFghmOD%0D%0ACvckeefUYlk5BZcp0uvFWWlVNA%2BuGzTIxHuj5P5Phdre7lkHgF60IKsG0VCvvcfUmbjSXwG0Ul3n%0D%0Abw6sJlyH4RqxAXILMbRGLi%2FYnaotHr2w4TKBJ%2FNxDy6QIUUmDhBz079EjEjUGLEKRNVcbMUUA4Hp%0D%0AkMuC6qyOUAzZ6UFpSPxPl%2FpGnxt4FCHcbXHF3Q2ZaOf4PvKDqyFSngXxpjIEfVJTRAubRxAwrTfr%0D%0AhMxcUDRaq5Ecnaso6K%2BlEMuToDDBmmL5P1Q%2FJisceC%2BtsbWOiAJqF9CPIWNMf%2BXIoQZ6afUcGyZk%0D%0AiEykPW46VoliiX2V2sEZd6CRfUUwAX9s0ddSBzk6ArvRDH6vzwUM6dZQ9j%2Fa9x9zGrv7Rfn8fp5B%0D%0AcjPEHe%2BkvFEu%2FqdLZunaPWKr1S%2FoE55E1qAcTL4c5E2EJrr7w267mTj35Nhb9VFQazDpvysTvr9J%0D%0Ahvjz%2F6NuClC0PJCXqfhnbpYD0uyLvkCKj9RGUAesMNNRy8WnKmJLZB0g8x5KytNT9FuBlljqQmWm%0D%0AL%2BDTz7kcW1NXhDGXcFeIDo2225HLgq%2Bj1UgoYkCcIZ0Z9D9smXDKPSX2i02S6f6KiIm4LrozTX1p%0D%0AKp%2FbatsWCxOcu3i2JlsgYdSbcSxNQW8xtsmcGLz4otU2Es2xIOj5ILw5Knumt%2BBjJO1PLEDgcN8O%0D%0AlAHG9QcMsCYlZS1LanC3OJKTgRD02HdVLGpTukpoyNp9bADLshbUuQFyjJHXYjU7rF7F28AId%2BYO%0D%0AkuiFJ0WUe037l2oy3IUmSs%2BISzUmhw1mDo0XPjyVJ3i%2B6KHeXUle8NeS56mZFWyKVoxu37rPD1AM%0D%0AEDbgz5CKwMMRsi%2FhwPCAX2oK6D%2FCgPzxHBJ6oKa8elqOlKOzH%2FzBNc51rHGAxiGh66c1Wvz5SC6m%0D%0AgUoAgNsSYVrofh1Obqdb%2FF8I57a1lvFO84KDO2llaqC8blaLPL9ceWlyOldkKf%2BOsRlIursBCQnF%0D%0AZUDHo%2BF%2Bd3BxmJERwqfqI726g%2BFv3HklgKXdZh8ibSp0LtFD7Lrh23PKOemQjhmGggBX4MbV5fm3%0D%0AVWeYqIoutziEZNZjGvWR%2B%2BxzcJhMe19OP8DQRqK6Xzu1spD5OTuFqXsALKiwEOf54Eq7qEZgrXlI%0D%0ArgZ6dlrt1JUG1sEaF535YGStohaz2GAHDoE1WKeB3FGOBSnz3goCqF%2BN%2BTKbKNq6A1%2Bjvhjmnoxe%0D%0A5EXvm6IL5skAIV3jAjZ7jkZjRIpHdHj%2FkIBVmUXz3vEULykbOW9SurXJZsH%2B8X3XKC8nA%2BOSYzgf%0D%0A7%2F0kl2BE2%2F7l5hGVnYu4C%2FnP81W4N8%2Ft39YKnRpXnpqE%2FZVDSmojn9gBYVqsbdgGgtWy%2BXN0hpDO%0D%0ATHERxCFx%2FHQhbARpqJ1UysFGs7ImdbWG23eS0GGIvGEcn34t6BgRbyQRN8GFaV%2B%2F89ifUVULyuHC%0D%0AxGuvIysRsWEfH5ch3Yowy9DJpJNSPrI33Ag5IySjqf0RgP97Cv4BPBBbhS1EzpzN%2BAV3dilJbbuN%0D%0A8GypBhiCS%2BcX%2F3syMGGrgSJbHIa8Dd9qU06LdXwsh2rmI1pETR%2FE1vjZ1TtS%2FxwwLVKI%2FXBuU%2FeZ%0D%0A8XyQkW3Ssd8%2FJtEMvKEqTrV199jCY53mV198tXM8odpxce7geDp4QIU9AQWimL6sYWrWnfKD97aH%0D%0A1S36sWZKP0TW7lmKlQhsSVY5wOEhre94rnTOpZAfYuRdzySSb9qmnx8fcgVtcY5%2BsXditqK6ZGaS%0D%0A0RXCG6Tmnu5wlt%2BZ7Jep%2BNYSxKKbL2HSc6PgkrPsawNGU8AxUG1ltpQ2BHAFud42tsJEZjw1wMzf%0D%0AAZGl3GMBm9yXhcHAe5vLH%2FIA6zAiCti4M77Q95dyLFRShFGL%2BHqAcm7jyUeWuk6Lc0wszxJl8wR%2F%0D%0A%2FcwWB3AePHjugFfup%2BiCgXquQFi19HMxLacQuqBNoSKJUfzRRRAtMvaHj%2FdRjLaY0mzxQXlN3h5r%0D%0AG0S%2FKFss5u%2FMfCvDvukQAH%2FQpanQMC7keG3UkyQ%2Fhof9dnCGpb9auWi73zIjFoyE9QoHQ93u2AqF%0D%0AJx1M97xOBg%3D%3D%5E%40%5E0.0.1
//            commonBean.userChooseMode:0
//            commonBean.phoneNo:13167081006
//            commonBean.provinceCode:031
//            commonBean.cityCode:310
//            rechargeMode:%E7%94%A8%E5%85%85%E5%80%BC%E5%8D%A1
//            cardBean.cardValueCode:04
//            offerPriceStrHidden:98.50
//            cardBean.cardValue:100
//            cardBean.minCardNum:1
//            cardBean.maxCardNum:3
//            MaxThreshold01:150
//            MinThreshold01:1
//            MaxThreshold02:10
//            MinThreshold02:1
//            MaxThreshold03:6
//            MinThreshold03:1
//            MaxThreshold04:3
//            MinThreshold04:1
//            MaxThreshold05:1
//            MinThreshold05:1
//            actionPayFeeInfo.actionConfigId:
//            commonBean.payAmount:98.50
//            invoiceBean.need_invoice:1
//            invoiceBean.invoice_type:
//            invoiceBean.is_mailing:1
//            saveflag:false
//            invoiceBean.post_list:
//            invoiceBean.invoice_list:
//            rechargeBean.cardPwd:9804053365833888635
//            commonBean.bussineType:


            url = "https://upay.10010.com/npfweb/NpfCellWeb/reCharge/reChargeApplay";
            HttpPost httpPost = new HttpPost(url);

//            param = String.format("secstate.state=%s&commonBean.userChooseMode=0&commonBean.phoneNo=%s&commonBean.provinceCode=%s&commonBean.cityCode=%s&rechargeMode=%s&cardBean.cardValueCode=04&offerPriceStrHidden=98.50&cardBean.cardValue=100&cardBean.minCardNum=1&cardBean.maxCardNum=3&MaxThreshold01=150&MinThreshold01=1&MaxThreshold02=10&MinThreshold02=1&MaxThreshold03=6&MinThreshold03=1&MaxThreshold04=3&MinThreshold04=1&MaxThreshold05=1&MinThreshold05=1&actionPayFeeInfo.actionConfigId=&commonBean.payAmount=98.50&invoiceBean.need_invoice=1&invoiceBean.invoice_type=&invoiceBean.is_mailing=1&saveflag=false&invoiceBean.post_list=&invoiceBean.invoice_list=&rechargeBean.cardPwd=%s&commonBean.bussineType="
//                    , secstate, phone, provinceCode, cityCode, "用充值卡", passCode);
//            Content-Type: application/x-www-form-urlencoded; charset=UTF-8
            //如果不加这个的话就会提示页面找不到的

//            param=MessageFormat.format("secstate.state={0}",secstate);

//            url = url + "?" + param;
//            encodeURL = new URL(url);
//            uri = new URI(encodeURL.getProtocol(), encodeURL.getUserInfo(), encodeURL.getHost(), encodeURL.getPort(), encodeURL.getPath(), encodeURL.getQuery(), null);


            httpPost.setEntity(new StringEntity(URLEncodedUtils.format(postParams,"UTF-8")));
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            result = EntityUtils.toString(client.execute(httpPost).getEntity());
            System.out.print("result:" + result);


//            httpPost = new HttpPost(uri);

            result = EntityUtils.toString(client.execute(httpPost).getEntity());
            System.out.print("result:" + result);

            url = "https://upay.10010.com/npfweb/NpfCellWeb/reCharge/reChargeConfirm";
            postParams = new ArrayList<NameValuePair>();
            postParams.add(new BasicNameValuePair("secstate.state", secstate));
            httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setEntity(new UrlEncodedFormEntity(postParams));
            result = EntityUtils.toString(client.execute(httpPost).getEntity(), "UTF-8");
            System.out.print("result:" + result);

            url = "https://upay.10010.com/npfweb/NpfCellWeb/reCharge/reChargeConfirm";
//            url= url+"secstate.state="+URLEncoder.encode(secstate,"UTF-8");
            httpPost = new HttpPost(url);
            httpPost.getParams().setParameter("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

            postParams = new ArrayList<NameValuePair>();
            postParams.add(new BasicNameValuePair("secstate.state", secstate));
            httpPost.setEntity(new UrlEncodedFormEntity(postParams));

            result = EntityUtils.toString(client.execute(httpPost).getEntity(), "UTF-8");

        } else {


        }


    }

}
