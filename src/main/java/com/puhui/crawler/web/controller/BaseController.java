package com.puhui.crawler.web.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.puhui.crawler.CreditReport;
import com.puhui.crawler.mobile.MobileFetcher;
import com.puhui.crawler.mobile.MobileFetcherMapper;

/**
 * basecontroller
 * 
 * @author zhuyuhang
 */
public class BaseController {

    private static final Logger logger = Logger.getLogger(BaseController.class);
    private static final String MOBILE_FETCHER_ATTR = MobileFetcher.class.getName() + ".class";
    private static final String CREDIT_REPORT_ATTR = CreditReport.class.getName() + ".class";
    @Resource
    private MobileFetcherMapper mobileFetcherMapper;

    /**
     * 被除了checkPhoneNumber之外的其它方法调用
     * 
     * @author zhuyuhang
     * @param request
     * @return
     */
    public MobileFetcher getMobileFetcher(HttpServletRequest request) {
        return getMobileFetcher(request, null, false);
    }

    /**
     * 应该只被 checkPhoneNumber方法调用
     * 
     * @author zhuyuhang
     * @param request
     * @param carrier
     * @param create
     * @return
     */
    public MobileFetcher getMobileFetcher(HttpServletRequest request, String carrier, boolean create) {
        MobileFetcher mobileFetcher = (MobileFetcher) request.getSession().getAttribute(MOBILE_FETCHER_ATTR);
        if (create) {
            if (mobileFetcher != null) {
                mobileFetcher.close();
                mobileFetcher = null;
            }
            String clazz = mobileFetcherMapper.getClassNameByCarrier(carrier);
            if (clazz == null) {
                return null;
            }
            try {
                mobileFetcher = (MobileFetcher) Class.forName(clazz).newInstance();
                request.getSession().setAttribute(MOBILE_FETCHER_ATTR, mobileFetcher);
                return mobileFetcher;
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return mobileFetcher;
    }

    public void removeMobileFetcher(HttpServletRequest request) {
        request.getSession().removeAttribute(MOBILE_FETCHER_ATTR);
    }

    /**
     * @author zhuyuhang
     * @param request
     * @param alwaysNew
     *            倘session里面有 则执行其 close方法后再创建
     * @return
     */
    public CreditReport getCreditReport(HttpServletRequest request, boolean alwaysNew) {
        CreditReport result = (CreditReport) request.getSession().getAttribute(CREDIT_REPORT_ATTR);
        if (alwaysNew && result != null) {
            result.close();
            result = null;
        }
        if (result == null) {
            result = new CreditReport();
            request.getSession().setAttribute(CREDIT_REPORT_ATTR, result);
        }
        return result;
    }

    public void removeCreditReport(HttpServletRequest request) {
        request.getSession().removeAttribute(CREDIT_REPORT_ATTR);
    }

    public String getBasePath(HttpServletRequest request) {
        String path = request.getContextPath();
        String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path;
        return basePath;
    }
}
