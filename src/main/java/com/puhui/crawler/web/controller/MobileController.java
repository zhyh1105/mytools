package com.puhui.crawler.web.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.puhui.crawler.mobile.MobileFetcher;
import com.puhui.crawler.mobile.MobileInfo;
import com.puhui.crawler.util.MobileInfoFetcher;

@Controller
@RequestMapping("/mobile")
public class MobileController extends BaseController {
    /**
     * 获取手机号信息
     * 
     * @author zhuyuhang
     * @param phone
     * @return
     */
    @ResponseBody
    @RequestMapping("/checkPhoneNumber")
    public MobileInfo checkPhoneNumber(HttpServletRequest request, @RequestParam(required = true) String phone) {
        MobileInfo mi = MobileInfoFetcher.getMobileInfo(phone);
        if (mi.isValidate()) {// 不是合法手机号
            MobileFetcher mobileFetcher = getMobileFetcher(request, mi.getCarrier(), true);
            if (mobileFetcher == null) {
                mi.setValidate(false);
                return mi;
            }
            mi.setCaptchaNeeded(mobileFetcher.hasCaptcha());
            mi.setRandomSmsCodeNeeded(mobileFetcher.hasRandomcode());
            mobileFetcher.setIsp(mi.getCatName());
            mobileFetcher.setArea(mi.getProvince());
        }
        return mi;
    }

    /**
     * 獲取驗證碼圖片
     * 
     * @author zhuyuhang
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping("/captcha")
    public ResponseEntity<byte[]> getCaptcha(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        MobileFetcher mobileFetcher = getMobileFetcher(request);
        File rnum = mobileFetcher.loadCaptchaCode();
        ResponseEntity<byte[]> result = new ResponseEntity<>(FileUtils.readFileToByteArray(rnum), headers,
                HttpStatus.OK);
        FileUtils.deleteQuietly(rnum);
        return result;
    }

    /**
     * 验证驗證碼
     * 
     * @author zhuyuhang
     * @param captchaCode
     * @return
     * @throws IOException
     */
    @RequestMapping("/checkCaptchaCode")
    @ResponseBody
    public boolean checkCaptchaCode(@RequestParam(required = true) String captchaCode) throws IOException {
        return true;
    }

    /**
     * 登錄對方系統
     * 
     * @author zhuyuhang
     * @param request
     * @param phone
     * @param password
     * @param rnum
     * @return
     */
    @RequestMapping("/login")
    @ResponseBody
    public boolean login(HttpServletRequest request, @RequestParam(required = true) String phone,
            @RequestParam(required = true) String password, @RequestParam(required = false) String rnum) {
        MobileFetcher mobileFetcher = getMobileFetcher(request);
        if (mobileFetcher == null) {
            return false;
        }
        return mobileFetcher.login(phone, password, rnum);
    }

    /**
     * 發送隨機驗證碼
     * 
     * @author zhuyuhang
     * @return
     */
    @ResponseBody
    @RequestMapping("/sendRandomcode")
    public boolean sendRandombySms(HttpServletRequest request) {
        MobileFetcher mobileFetcher = getMobileFetcher(request);
        if (mobileFetcher == null) {
            return false;
        }
        return mobileFetcher.sendRandombySms();
    }

    /**
     * 驗證隨機驗證碼
     * 
     * @author zhuyuhang
     * @return
     */
    @ResponseBody
    @RequestMapping("/validateRandomcode")
    public boolean validateRandomcode(HttpServletRequest request, @RequestParam(required = true) String randomCode) {
        MobileFetcher mobileFetcher = getMobileFetcher(request);
        if (mobileFetcher == null) {
            return false;
        }
        return mobileFetcher.validateRandomcode(randomCode);
    }

    /**
     * 獲取賬單
     * 
     * @author zhuyuhang
     * @param request
     * @param phone
     * @param password
     * @param rnum
     * @return
     */
    @RequestMapping("/bills")
    @ResponseBody
    public boolean getBills(HttpServletRequest request) {
        MobileFetcher mobileFetcher = getMobileFetcher(request);
        if (mobileFetcher == null) {
            return false;
        }
        try {
            return mobileFetcher.loadBills();
        } finally {// 加载完后移除session
            removeMobileFetcher(request);
        }
    }
}
