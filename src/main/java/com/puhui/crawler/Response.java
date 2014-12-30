package com.puhui.crawler;

public class Response {
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
    /**
     * 结果
     * 
     * @author zhuyuhang
     */
    private Object result;

    public Response() {

    }

    public Response(boolean result, String errorCode) {
        this(result, errorCode, null);
    }

    public Response(boolean result, String errorCode, String errorMsg) {
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

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

}
