package com.fqm.framework.common.lock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fqm.framework.common.lock.template.LockTemplate;

/**
 * 分布式锁 
 * Lock For Java
 * 
 * @version 
 * @author 傅泉明
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Lock4j {

    /**
     * 锁模板
     * @return
     */
    @SuppressWarnings("rawtypes")
    Class<? extends LockTemplate> lockTemplate() default LockTemplate.class;

    /**
     * support SPEL expresion 锁的key
     *
     * @return KEY
     */
    String key() default "";
    
    /**
     * 是否阻塞线程，调用{@link Lock.lock()}方法
     * @return
     */
    boolean block() default false;

    /**
     * support SPEL expresion
     * @return 过期时间 单位：毫秒
     * <pre>
     *     过期时间一定是要长于业务的执行时间. 未设置则为默认时间3秒
     * </pre>
     */
    long expire() default 3000;

    /**
     * support SPEL expresion
     * @return 获取锁超时时间 单位：毫秒
     * =0：tryLock，尝试获取一次锁
     * >0：tryLock，在指定时间内获取锁，调用{@link Lock.tryLock(long timeout, TimeUnit unit)}方法
     * <pre>
     *     未设置则为默认时间0秒 默认值：调用{@link Lock.tryLock()}方法
     * </pre>
     */
    long acquireTimeout() default 0;
    
}
