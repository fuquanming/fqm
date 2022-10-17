package com.fqm.framework.common.core.exception.enums;

import com.fqm.framework.common.core.exception.ErrorCode;

/**
 * 全局错误码枚举
 * 0-999 系统异常编码保留
 *
 * 一般情况下，使用 HTTP 响应状态码 https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Status
 * 虽然说，HTTP 响应状态码作为业务使用表达能力偏弱，但是使用在系统层面还是非常不错的
 * 
 * @version 
 * @author 傅泉明
 */
public interface GlobalErrorCodeConstants {

    ErrorCode SUCCESS = new ErrorCode(200, "成功");

    // ========== 客户端错误段 ==========

    ErrorCode BAD_REQUEST = new ErrorCode(400, "请求参数不正确");

    ErrorCode UNAUTHORIZED = new ErrorCode(401, "账号未登录");

    ErrorCode FORBIDDEN = new ErrorCode(403, "没有该操作权限");

    ErrorCode NOT_FOUND = new ErrorCode(404, "请求未找到");

    ErrorCode METHOD_NOT_ALLOWED = new ErrorCode(405, "请求方法不正确");

    // ========== 服务端错误段 ==========

    ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode(500, "系统异常");

    ErrorCode UNKNOWN = new ErrorCode(999, "未知错误");

    /**
     * 是否是errorCode
     * @param code
     * @return
     */
    static boolean isMatch(Integer code) {
        return code != null && code.intValue() >= SUCCESS.getCode().intValue() && code.intValue() <= UNKNOWN.getCode().intValue();
    }

}
