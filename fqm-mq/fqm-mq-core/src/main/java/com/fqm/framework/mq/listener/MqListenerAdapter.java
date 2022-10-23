package com.fqm.framework.mq.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.framework.common.core.util.JsonUtil;
import com.fqm.framework.mq.exception.MqException;
import com.google.common.base.Preconditions;

/**
 * 监听消息队列适配器
 * 
 * @version 
 * @author 傅泉明
 */
public class MqListenerAdapter<T> implements MqListener<T> {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Method method;
    private Object bean;
    
    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }
    
    public MqListenerAdapter(Method method, Object bean) {
        this.method = method;
        this.bean = bean;
    }

    @Override
    public void receiveMessage(T message) throws MqException {
        try {
            int parameterCount = method.getParameterCount();
            Preconditions.checkArgument(parameterCount == 1, "The number of @MqListener listening method parameters must be 1");
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> clazz = parameterTypes[0];
            if (clazz == String.class) {
                method.invoke(bean, message);
            } else {
                // 自定义类型转换
                logger.debug("Object={},method={},receiveMessage={}", bean.getClass(), method.getName(), message);
                Object obj = JsonUtil.toBean(message.toString(), clazz);
                method.invoke(bean, obj);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new MqException(e);
        }
    }
}
