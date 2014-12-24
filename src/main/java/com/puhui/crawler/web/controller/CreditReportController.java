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

import com.amos.tool.PropertiesUtil;
import com.puhui.crawler.CreditReport;

@Controller
@RequestMapping("/creditReport")
public class CreditReportController extends BaseController {
    private static final boolean isCaptchaNeeded = Boolean.valueOf(
            PropertiesUtil.getProps("credit.report.is.captcha.needed")).booleanValue();

    /**
     * 獲取驗證碼圖片
     * 
     * @author zhuyuhang
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping("/isCaptchaNeeded")
    @ResponseBody
    public boolean isCaptchaNeeded() {
        return isCaptchaNeeded;
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
        CreditReport creditReport = getCreditReport(request, true);
        File rnum = creditReport.loadCaptchaCode();
        ResponseEntity<byte[]> result = new ResponseEntity<>(FileUtils.readFileToByteArray(rnum), headers,
                HttpStatus.OK);
        FileUtils.deleteQuietly(rnum);
        return result;
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
    public boolean login(HttpServletRequest request, @RequestParam(required = true) String loginname,
            @RequestParam(required = true) String password, @RequestParam(required = true) String tradeCode,
            @RequestParam(required = false) String captchaCode) {
        CreditReport creditReport = getCreditReport(request, false);
        if (creditReport == null) {
            return false;
        }
        try {
            return creditReport.loadCreditReport(loginname, password, tradeCode, captchaCode);
        } finally {
            creditReport.close();
            removeCreditReport(request);
        }
    }
}
