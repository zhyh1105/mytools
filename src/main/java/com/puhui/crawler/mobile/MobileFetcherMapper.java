package com.puhui.crawler.mobile;

import java.util.Map;

public class MobileFetcherMapper {
    private Map<String, String> holder;

    public void setHolder(Map<String, String> holder) {
        this.holder = holder;
    }

    /**
     * 根据 carrier获取对应的类 <br>
     * 如 北京电信 --> com.puhui.crawler.mobile.CT_BJ_MobileFetcher
     * 
     * @author zhuyuhang
     * @param carrier
     * @return
     */
    public String getClassNameByCarrier(String carrier) {
        if (carrier.contains("联通")) {
            return holder.get("联通");
        }
        return holder.get(carrier);
    }
}
