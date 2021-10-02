package com.fqm.framework.web.core.handler;

import java.io.IOException;
import java.lang.reflect.Type;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import com.fqm.framework.common.core.util.JsonUtil;

/**
 * 全局接收结果（RequestBody）处理器
 * 
 * @version 
 * @author 傅泉明
 */
@ControllerAdvice
public class GlobalRequestBodyHandler implements RequestBodyAdvice {

    /**
     * 返回true表示拦截 
     * @see org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice#supports(org.springframework.core.MethodParameter, java.lang.reflect.Type, java.lang.Class)
     *
     */
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        return inputMessage;
    }

    /**
     * 读取完成后处理
     * @see org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice#afterBodyRead(java.lang.Object, org.springframework.http.HttpInputMessage, org.springframework.core.MethodParameter, java.lang.reflect.Type, java.lang.Class)
     *
     */
    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        System.out.println("afterBodyRead=" + JsonUtil.toJsonStr(body));
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

}
