package com.puhui.crawler.web.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

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
}
