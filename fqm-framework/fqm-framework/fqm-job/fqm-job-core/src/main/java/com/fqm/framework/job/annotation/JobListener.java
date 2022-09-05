package com.fqm.framework.job.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 任务监听器
 * 
 * @version 
 * @author 傅泉明
 */
@Target(value = { ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface JobListener {
    /** 任务名称 */
    String value();
    /** @JobMode */
    String binder() default "";
}
