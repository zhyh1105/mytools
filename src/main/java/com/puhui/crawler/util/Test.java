package com.puhui.crawler.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        String regex = ".*(webmaster@icbc\\.com\\.cn|admin@creditcard\\.hxb\\.com\\.cn|ccsvc@message\\.cmbchina\\.com|citiccard@citiccard\\.com|service@vip\\.ccb\\.com|estmtservice@eb\\.spdbccc\\.com\\.cn|creditcard@cgbchina\\.com\\.cn|PersonalService@bank-of-china\\.com).*";
        String regex2 = ".*(中国工商银行客户对账单|华夏银行信用卡-电子账单|招商银行信用卡电子账单|中信银行信用卡电子账单|中国建设银行信用卡电子账单|浦发银行-信用卡电子账单|广发卡.*电子对账单|中国银行银行卡电子账单).*";
        Pattern pattern = Pattern.compile(regex);
        Pattern p2 = Pattern.compile(regex2);
        // 尊敬的李大州先生，广发卡2014年10月电子对账单
        Matcher m = p2.matcher("尊敬的李大州先生，广发卡2014年10月电子对账单");
        System.out.println(m.matches());

        String str1 = "Collection of tutorials";
        String str2 = "Consists of different tutorials";

        /*
         * matches characters from index 14 in str1 to characters from index 22
         * in str2 considering same case of the letters
         */
        boolean match1 = str1.regionMatches(0, str2, 0, 22);
        System.out.println("region matched = " + match1);
    }
}
