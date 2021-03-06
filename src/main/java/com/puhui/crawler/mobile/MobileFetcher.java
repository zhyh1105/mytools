package com.puhui.crawler.mobile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.amos.tool.PropertiesUtil;
import com.puhui.crawler.util.DateUtils;
import com.puhui.crawler.util.HttpUtils;

/**
 * 手机账单获取器
 * 
 * @author zhuyuhang
 */
public abstract class MobileFetcher {
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    protected boolean loadBillsSuccessfully = false;
    protected final List<Future<Boolean>> futures = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(MobileFetcher.class);
    protected static final int PAGE_SIZE = NumberUtils.toInt(PropertiesUtil.getProps("mobile.bills.count.per.page"),
            100000);
    protected static final int MOBILE_BILLS_MONTH_COUNT = NumberUtils.toInt(
            PropertiesUtil.getProps("mobile.bills.month.count"), 6);
    /**
     * <pre>
     * 中国移动 cm
     * 中国联通 cu
     * 中国电信 ct
     * </pre>
     * 
     * @author zhuyuhang
     */
    protected static final String ISP_CM = "cm", ISP_CU = "cu", ISP_CT = "ct";
    /**
     * <pre>
     * 历史账单 hisbill
     * 通话详单 gsm
     * 短信详单 sms
     * 个人信息 personalinfo
     * 代收业务扣费记录 mon
     * 实时话费 currFee
     * 套餐及固定费详单 rc
     * 上网流量 gprs
     * 增值业务扣费记录 addvalue
     * </pre>
     * 
     * @author zhuyuhang
     */
    protected static final String BILL_TYPE_HISBILL = "hisbill", BILL_TYPE_GSM = "gsm", BILL_TYPE_SMS = "sms",
            BILL_TYPE_PERSONALINFO = "personalinfo", BILL_TYPE_MON = "mon", BILL_TYPE_GPRS = "gprs",
            BILL_TYPE_RC = "rc", BILL_TYPE_CURRFEE = "currfee", BILL_TYPE_ADDVALUE = "addvalue",
            BILL_TYPE_ACCOUNTBALANCE = "accountBalance", BILL_TYPE_ADDRESS = "address";
    /**
     * 手機號
     * 
     * @author zhuyuhang
     */
    private String phone;
    /**
     * 服務密碼
     * 
     * @author zhuyuhang
     */
    private String password;
    /**
     * 驗證碼
     * 
     * @author zhuyuhang
     */
    private String captchaCode;
    /**
     * 隨機短信
     * 
     * @author zhuyuhang
     */
    private String randomCode;
    /**
     * 運營商
     * 
     * @author zhuyuhang
     */
    private String isp;
    /**
     * 地區
     * 
     * @author zhuyuhang
     */
    private String area;

    /**
     * 是否有登錄驗證碼 默认返回false
     * 
     * @author zhuyuhang
     * @return
     */
    public boolean hasCaptcha() {
        return false;
    }

    /**
     * 是否含有二次图片验证码，即登录后获取随机短信之前
     * 
     * @author zhuyuhang
     * @return
     */
    public boolean hasSecondCaptcha() {
        return false;
    }

    /**
     * 獲取驗證碼圖片
     * 
     * @author zhuyuhang
     * @return
     */
    public File loadCaptchaCode() {
        return null;
    }

    /**
     * 二次图片验证码，即登录后获取随机短信之前
     * 
     * @author zhuyuhang
     * @return
     */
    public File loadSencondCaptchaCode() {
        return null;
    }

    /**
     * 获取登录验证码图片
     * 
     * @author zhuyuhang
     * @param client
     * @param url
     * @return
     */
    protected File getCaptchaCodeImage(CloseableHttpClient client, String url) {
        try {
            HttpGet get = HttpUtils.get(url);
            CloseableHttpResponse response = client.execute(get);
            File codeFile = new File(PropertiesUtil.getProps("mobile.captcha.dir"), System.currentTimeMillis() + ".jpg");
            FileUtils.copyInputStreamToFile(response.getEntity().getContent(), codeFile);
            response.close();
            logger.debug("获取验证码");
            return codeFile;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 驗證驗證碼 默认返回false
     * 
     * @author zhuyuhang
     * @return
     */
    public boolean checkCaptchaCode(String captchaCode) {
        return true;
    }

    /**
     * 二次图片验证 默认返回true
     * 
     * @author zhuyuhang
     * @param captchaCode
     * @return
     */
    public boolean checkSecondCaptchaCode(String captchaCode) {
        return true;
    }

    /**
     * 登錄
     * 
     * @author zhuyuhang
     * @param phone
     * @param password
     * @param rnum
     * @return
     */
    public boolean login(String phone, String password, String rnum) {
        this.phone = phone;
        this.password = password;
        this.captchaCode = rnum;
        return true;
    }

    /**
     * 登录后是否含有随机短信验证码
     * 
     * @author zhuyuhang
     * @return
     */
    public boolean hasRandomcode() {
        return false;
    }

    /**
     * 发送随机短信验证码
     * 
     * @author zhuyuhang
     * @return
     */
    public boolean sendRandombySms() {
        return false;
    }

    /**
     * 验证随机短信验证码
     * 
     * @author zhuyuhang
     * @param randomCode
     * @return
     */
    public boolean validateRandomcode(String randomCode) {
        return false;
    }

    /**
     * 获取账单 默认返回false
     * 
     * @author zhuyuhang
     * @return
     */
    public boolean loadBills() {
        return false;
    }

    protected File createTempFile(String type) {
        String billsDir = PropertiesUtil.getProps("mobile.bills.dir");
        File file = new File(billsDir, DateUtils.formatDate(new Date(), "yyyyMMdd"));
        if (!file.exists() || !file.isDirectory()) {
            file.mkdir();
        }
        file = new File(file, this.phone);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdir();
        }
        // 185500492821_cu_bj_type_System.currentTimeMillis().html
        // 185500492821_cu_bj_type.3g.System.currentTimeMillis().html
        String name = null;
        if (getIspSimpleName().equals(ISP_CU)) {
            name = getPhone() + "_" + getIspSimpleName() + "_" + type + "_" + System.currentTimeMillis() + ".html";
        } else {
            name = getPhone() + "_" + getIspSimpleName() + "_" + getAreaSimpleName() + "_" + type + "_"
                    + System.currentTimeMillis() + ".html";
        }
        name = name.toLowerCase();
        file = new File(file, name);
        return file;
    }

    protected String writeToFile(File file, HttpEntity entity) throws UnsupportedEncodingException, ParseException,
            IOException {
        String content = EntityUtils.toString(entity, HttpUtils.UTF_8);
        FileUtils.write(file, content, HttpUtils.UTF_8);
        return content;
    }

    protected String writeToFile(File file, String content) throws UnsupportedEncodingException, ParseException,
            IOException {
        FileUtils.write(file, content, HttpUtils.UTF_8);
        return content;
    }

    /**
     * 獲取手機號
     * 
     * @author zhuyuhang
     * @return
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 設置手機號
     * 
     * @author zhuyuhang
     * @param phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 獲取服务密碼
     * 
     * @author zhuyuhang
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * 設置服务密碼
     * 
     * @author zhuyuhang
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 獲取驗證碼
     * 
     * @author zhuyuhang
     * @return
     */
    public String getCaptchaCode() {
        return captchaCode;
    }

    /**
     * 設置驗證碼
     * 
     * @author zhuyuhang
     * @param captchaCode
     */
    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    /**
     * 获取随机短信验证码
     * 
     * @author zhuyuhang
     * @return
     */
    public String getRandomCode() {
        return randomCode;
    }

    /**
     * 設置随机短信验证码
     * 
     * @author zhuyuhang
     * @param randomCode
     */
    public void setRandomCode(String randomCode) {
        this.randomCode = randomCode;
    }

    /**
     * 獲取運營商
     * 
     * @author zhuyuhang
     * @return
     */
    public String getIsp() {
        return isp;
    }

    /**
     * 設置運營商
     * 
     * @author zhuyuhang
     * @param isp
     */
    public void setIsp(String isp) {
        this.isp = isp;
    }

    /**
     * 獲取地區
     * 
     * @author zhuyuhang
     * @return
     */
    public String getArea() {
        return area;
    }

    /**
     * 設置地區
     * 
     * @author zhuyuhang
     * @param area
     */
    public void setArea(String area) {
        this.area = area;
    }

    /**
     * isp 简写 <br>
     * 
     * <pre>
     * 中国联通 cu
     * 中国移动 cm
     * 中国电信 ct
     * </pre>
     * 
     * @author zhuyuhang
     * @return
     */
    public abstract String getIspSimpleName();

    /**
     * 地区简写 *
     * 
     * <pre>
     * 北京 bj
     * 重庆 cq
     * 湖南 hn
     * 湖北 hb
     * </pre>
     * 
     * @author zhuyuhang
     * @return
     */
    public abstract String getAreaSimpleName();

    /**
     * 个人信息
     * 
     * @author zhuyuhang
     */
    protected void personalInfo() {
    }

    /**
     * 当前话费
     * 
     * @author zhuyuhang
     */
    protected void currFee() {
    }

    /**
     * 历史账单
     * 
     * @author zhuyuhang
     */
    protected void hisBill() {
    }

    /**
     * 通话详单
     * 
     * @author zhuyuhang
     */
    protected void gsm() {
    }

    /**
     * 短信详单
     * 
     * @author zhuyuhang
     */
    protected void sms() {
    }

    /**
     * 充值详单
     * 
     * @author zhuyuhang
     */
    protected void mzlog() {
    };

    /**
     * 增值详单
     * 
     * @author zhuyuhang
     */
    protected void addvalue() {
    }

    /**
     * 套餐及固定费
     * 
     * @author zhuyuhang
     */
    protected void rc() {
    }

    /**
     * 上网流量
     * 
     * @author zhuyuhang
     */
    protected void gprs() {
    }

    /**
     * 代收费用
     * 
     * @author zhuyuhang
     */
    protected void mon() {
    }

    /**
     * 余额及欠费
     * 
     * @author zhuyuhang
     */
    protected void accountBalance() {

    }

    /**
     * 收货地址
     * 
     * @author zhuyuhang
     */
    protected void address() {

    }

    /**
     * 添加到线程
     * 
     * @author zhuyuhang
     */
    protected void submitBillTasks() {
        futures.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                currFee();
                return true;
            }
        }));
        futures.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                personalInfo();
                return true;
            }
        }));
        futures.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                hisBill();
                return true;
            }
        }));
        futures.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                gsm();
                return true;
            }
        }));
        futures.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                sms();
                return true;
            }
        }));
        futures.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mzlog();
                return true;
            }
        }));
        futures.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                addvalue();
                return true;
            }
        }));
        futures.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                rc();
                return true;
            }
        }));
        futures.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                gprs();
                return true;
            }
        }));
        futures.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mon();
                return true;
            }
        }));
        futures.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                accountBalance();
                return true;
            }
        }));
        futures.add(EXECUTOR_SERVICE.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                address();
                return true;
            }
        }));
    }

    /**
     * 执行完任务后 关闭资源
     * 
     * @author zhuyuhang
     */
    public void close() {
        for (Future<Boolean> f : futures) {
            try {
                f.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        futures.clear();
    }
}
