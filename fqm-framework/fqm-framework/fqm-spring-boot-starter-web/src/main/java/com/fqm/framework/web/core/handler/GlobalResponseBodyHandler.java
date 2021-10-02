package com.fqm.framework.web.core.handler;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.fqm.framework.common.core.vo.R;

/**
 * 全局响应结果（ResponseBody）处理器
 *
 * 目前，GlobalResponseBodyHandler 的主要作用是，记录 Controller 的返回结果，
 * 方便 {@link com.fqm.framework.web.core.interceptor.AccessLogInterceptor} 记录访问日志
 * @version 
 * @author 傅泉明
 */
@ControllerAdvice
public class GlobalResponseBodyHandler implements ResponseBodyAdvice {

    @Override
    @SuppressWarnings("NullableProblems") // 避免 IDEA 警告
    public boolean supports(MethodParameter returnType, Class converterType) {
        if (returnType.getMethod() == null) {
            return false;
        }
        // 只拦截返回结果为 R 类型
        return returnType.getMethod().getReturnType() == R.class;
    }

    @Override
    @SuppressWarnings("NullableProblems") // 避免 IDEA 警告
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // 记录 Controller 结果
//        CommonWebUtil.setCommonResult(((ServletServerHttpRequest) request).getServletRequest(), (R<?>) body);
        ((ServletServerHttpRequest) request).getServletRequest().setAttribute("fqm_common_result", (R<?>) body);
        return body;
    }
    
}
