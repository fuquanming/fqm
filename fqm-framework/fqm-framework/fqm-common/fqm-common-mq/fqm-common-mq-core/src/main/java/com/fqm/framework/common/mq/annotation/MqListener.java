package com.fqm.framework.common.mq.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * mq消息监听
 * 
 * @version 
 * @author 傅泉明
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface MqListener {
    /** 设置当前的消费者数量 */
    int concurrentConsumers() default 1;
    /** @MqMode 类型:kafka,rabbit,redis,rocket,zookeeper */
    String binder() default "";
    /** 主题topic */
    String destination() default "";
    /** 消费分组 */
    String group() default "";
}
