package com.fqm.framework.common.core.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * 
 * @version 
 * @author 傅泉明
 */
public class ExceptionUtil {
    
    private ExceptionUtil() {
    }
    
    public static String getMessage(Throwable th) {
        return ExceptionUtils.getMessage(th);
    }

    public static String getRootCauseMessage(Throwable th) {
        return ExceptionUtils.getRootCauseMessage(th);
    }
    
    public static String getStackTrace(Throwable th) {
        return ExceptionUtils.getStackTrace(th);
    }
}
