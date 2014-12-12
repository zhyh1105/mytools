package com.puhui.crawler.util;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.puhui.crawler.mobile.MobileInfo;

/**
 * 手机号信息获取
 * 
 * @author zhuyuhang
 */
public class MobileInfoFetcher {
    public static MobileInfo getMobileInfo(String phone) {
        try {
            String result = HttpUtils.executeGetWithResult(HttpUtils.getHttpClient(),
                    "http://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=" + phone);
            result = result.replace("__GetZoneResult_ =", "").trim();
            MobileInfo mi = JSON.parseObject(result, MobileInfo.class);
            if (StringUtils.isNotBlank(mi.getCatName())) {
                mi.setValidate(true);
            }
            return mi;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MobileInfo();
    }
}
