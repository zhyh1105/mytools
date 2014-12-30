package com.puhui.crawler.mobile;

public class MobileInfo {
    private boolean validate = false;
    private boolean captchaNeeded;
    private boolean secondCaptchaNeeded;
    private boolean randomSmsCodeNeeded;
    private String mts;
    private String province;
    private String catName;
    private String telString;
    private String areaVid;
    private String ispVid;
    private String carrier;

    /**
     * 手机号正确与否 通过淘宝验证
     * 
     * @author zhuyuhang
     * @return
     */
    public boolean isValidate() {
        return validate;
    }

    /**
     * 手机号正确与否
     * 
     * @author zhuyuhang
     * @param validate
     */
    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    /**
     * 是否需要验证码
     * 
     * @author zhuyuhang
     * @return
     */
    public boolean isCaptchaNeeded() {
        return captchaNeeded;
    }

    /**
     * 是否需要验证码
     * 
     * @author zhuyuhang
     * @param captchaNeeded
     */
    public void setCaptchaNeeded(boolean captchaNeeded) {
        this.captchaNeeded = captchaNeeded;
    }

    /**
     * 是否需要随机短信密码
     * 
     * @author zhuyuhang
     * @return
     */
    public boolean isRandomSmsCodeNeeded() {
        return randomSmsCodeNeeded;
    }

    /**
     * 是否需要随机短信密码
     * 
     * @author zhuyuhang
     * @param randomSmsCodeNeeded
     */
    public void setRandomSmsCodeNeeded(boolean randomSmsCodeNeeded) {
        this.randomSmsCodeNeeded = randomSmsCodeNeeded;
    }

    /**
     * 手机号前七位 如1530107
     * 
     * @author zhuyuhang
     * @return
     */
    public String getMts() {
        return mts;
    }

    /**
     * 手机号前七位 如1530107
     * 
     * @author zhuyuhang
     * @param mts
     */
    public void setMts(String mts) {
        this.mts = mts;
    }

    /**
     * 省份 如北京
     * 
     * @author zhuyuhang
     * @return
     */
    public String getProvince() {
        return province;
    }

    /**
     * 省份 如北京
     * 
     * @author zhuyuhang
     * @param province
     */
    public void setProvince(String province) {
        this.province = province;
    }

    /**
     * 如 北京电信
     * 
     * @author zhuyuhang
     * @return
     */
    public String getCatName() {
        return catName;
    }

    /**
     * 如 北京电信
     * 
     * @author zhuyuhang
     * @param catName
     */
    public void setCatName(String catName) {
        this.catName = catName;
    }

    /**
     * 如 15301070968
     * 
     * @author zhuyuhang
     * @return
     */
    public String getTelString() {
        return telString;
    }

    /**
     * 15301070968
     * 
     * @author zhuyuhang
     * @param telString
     */
    public void setTelString(String telString) {
        this.telString = telString;
    }

    /**
     * 如29400
     * 
     * @author zhuyuhang
     * @return
     */
    public String getAreaVid() {
        return areaVid;
    }

    /**
     * 如29400
     * 
     * @author zhuyuhang
     * @param areaVid
     */
    public void setAreaVid(String areaVid) {
        this.areaVid = areaVid;
    }

    /**
     * 如138238560
     * 
     * @author zhuyuhang
     * @return
     */
    public String getIspVid() {
        return ispVid;
    }

    /**
     * 如138238560
     * 
     * @author zhuyuhang
     * @param ispVid
     */
    public void setIspVid(String ispVid) {
        this.ispVid = ispVid;
    }

    /**
     * 如北京电信
     * 
     * @author zhuyuhang
     * @return
     */
    public String getCarrier() {
        return carrier;
    }

    /**
     * 如北京电信
     * 
     * @author zhuyuhang
     * @param carrier
     */
    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    /**
     * 是否需要二次图片验证码
     * 
     * @author zhuyuhang
     * @return
     */
    public boolean isSecondCaptchaNeeded() {
        return secondCaptchaNeeded;
    }

    /**
     * 是否需要二次图片验证码
     * 
     * @author zhuyuhang
     * @param secondCaptchaNeeded
     */
    public void setSecondCaptchaNeeded(boolean secondCaptchaNeeded) {
        this.secondCaptchaNeeded = secondCaptchaNeeded;
    }

}
