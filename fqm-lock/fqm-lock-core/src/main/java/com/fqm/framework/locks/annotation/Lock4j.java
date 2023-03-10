package com.fqm.framework.locks.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
     * 锁的业务名称，唯一，对应配置文件 lock.locks.xx，使用其配置的（name、binder、block、acquireTimeout）和 优先级小于属性 key、block、acquireTimeout
     * @return name
     */
    String name() default "";
    
    /**
     * support SPEL expresion 锁的名称
     * @return name
     */
    String key() default "";
    
    /**
     * 是否阻塞线程，true则调用{@link Lock.lock()}方法
     * @return
     */
    boolean block() default false;

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
