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
     * 锁的业务名称，唯一，对应配置文件 lock.locks.xx，使用其配置的（name、binder、block、acquireTimeout）和 配置优先级小于属性 key、acquireTimeout
     * @return name
     */
    String name() default "";
    
    /**
     * support SPEL expresion 锁的名称
     * 方法参数     #param.name                         ✅   已支持
     * 当前对象字段  #this.xxx                            ✅   rootObject
     * 配置值占位符  ${server.port}                       ✅   placeholderHelper
     * Spring Bean 方法调用    @bean.method(...)        ✅   BeanFactoryResolver
     * 混合表达式   `"... + #param + @bean.xxx + ${}``   ✅   综合支持
     * 
     * 例如：
     * @Lock4j(key = "'user:' + #testUser.id + '-' + @userService.testName(#testUser.id) + '-'  + #this.testName + '-' + ${server.port}")
     * public Object getUserByLock4jLock(TestUser testUser) {}
     * 
     * @return name
     */
    String key() default "";
    
    /**
     * 获取锁超时时间
     * <0：lock，阻塞获取锁，调用{@link Lock.lock()}方法
     * =0：tryLock，尝试获取一次锁，调用{@link Lock.tryLock()}方法
     * >0：tryLock，在指定时间内获取锁，调用{@link Lock.tryLock(long timeout, TimeUnit unit)}方法
     * <pre>
     *     未设置则为默认时间Long.MIN_VALUE秒 默认值：调用{@link Lock.lock()}方法
     * </pre>
     * @return 获取锁超时时间 单位：毫秒
     */
    long acquireTimeout() default Long.MIN_VALUE;
    
}
