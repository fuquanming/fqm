package com.fqm.framework.mq.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * mq消息监听
 * @version 
 * @author 傅泉明
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface MqListener {
    /** 消息业务名称，唯一，对应配置文件 mq.mqs.xx，使用其配置的（topic、group、binder）和 优先级小于属性 topic、group  */
    String name() default "";
    /** 设置当前的消费者数量 */
    int concurrentConsumers() default 1;
    /** 主题topic，则 @MqMode 取配置文件 mq.binder */
    String topic() default "";
    /** 消费分组，则 @MqMode 取配置文件 mq.binder */
    String group() default "";
}
