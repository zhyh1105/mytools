package com.puhui.crawler;

public class Result {
    /**
     * 结果
     * 
     * @author zhuyuhang
     */
    private boolean success = false;
    /**
     * 错误码
     * 
     * @author zhuyuhang
     */
    private String errorCode = "";
    /**
     * 错误信息
     * 
     * @author zhuyuhang
     */
    private String errorMsg = "";

    public Result() {

    }

    public Result(boolean result, String errorMsg) {
        this(result, null, errorMsg);
    }

    public Result(boolean result, String errorCode, String errorMsg) {
        this.success = result;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

}
