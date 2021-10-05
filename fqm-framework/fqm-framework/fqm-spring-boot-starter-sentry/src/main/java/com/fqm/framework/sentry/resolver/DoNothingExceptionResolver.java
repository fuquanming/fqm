package com.fqm.framework.sentry.resolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import io.sentry.IHub;
import io.sentry.spring.SentryExceptionResolver;

/**
 * 默认什么也不做的 SentryExceptionResolver
 * 
 * @version 
 * @author 傅泉明
 */
public class DoNothingExceptionResolver extends SentryExceptionResolver {

    public DoNothingExceptionResolver(IHub hub, int order) {
        super(hub, order);
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    public ModelAndView resolveException(final HttpServletRequest request, final HttpServletResponse response, final Object handler,
            final Exception ex) {

        return null;
    }

}