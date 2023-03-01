package com.fqm.framework.mq.annotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.config.MqConfigurationProperties;
import com.fqm.framework.mq.config.MqProperties;
import com.fqm.framework.mq.listener.MqListenerParam;

/**
 * @MqListener 注解监听，并转换为 List<MqListenerParam> 对象
 * 参考 RabbitListenerAnnotationBeanPostProcessor
 * 拦截 @ConfigurationProperties 配置为 mq.mqs 的所有配置，为@MqListener 中使用#{}，调用 @ConfigurationProperties 的方法准备，
 * 如果 @ConfigurationProperties 下的bean 通过 @Component 等注入到工厂，则beanName为类名：xxxProperties,
 * 如果 @ConfigurationProperties 下的bean 没有 @Component 等注入到工厂，则beanName为配置前缀+类全包名：mq.mqs.a-xx.xx.xxxProperties
 * @version 
 * @author 傅泉明
 */
public class MqListenerAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {

    /** 获取使用 @MqListener 的类及方法  */
    private Map<MqMode, List<MqListenerParam>> listenerParams = new EnumMap<>(MqMode.class);
    
    private MqProperties mqProperties;
    
    public MqListenerAnnotationBeanPostProcessor(MqProperties mqProperties) {
        this.mqProperties = mqProperties;
    }
    
    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        final List<ListenerMethod> methods = new ArrayList<>();
        ReflectionUtils.doWithMethods(targetClass, method -> {
            Collection<MqListener> listenerAnnotations = findListenerAnnotations(method);
            if (!listenerAnnotations.isEmpty()) {
                methods.add(new ListenerMethod(method, listenerAnnotations.toArray(new MqListener[listenerAnnotations.size()])));
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);
        
        if (!methods.isEmpty()) {
            for (ListenerMethod method : methods) {
                for (MqListener mqListener : method.mqListener) {
                    buildMqListenerParam(bean, method, mqListener);
                }
            }
        }

        return bean;
    }

    @SuppressWarnings("unchecked")
    private void buildMqListenerParam(final Object bean, ListenerMethod method, MqListener mqListener) {
        // 消息的业务名称
        String name = mqListener.name();
        // 消息主题
        String topic = mqListener.topic();
        // 消费者组
        String group = mqListener.group();
        boolean nameFlag = StringUtils.hasText(name);
        boolean topicFlag = StringUtils.hasText(topic);
        boolean groupFlag = StringUtils.hasText(group);
        // 1、设置消息主题、消费者组
        if (topicFlag && groupFlag) {
            MqMode mqMode = mqProperties.getBinder();
            Assert.isTrue(null != mqMode, "Please specific [binder] under [mq] configuration.");
            MqListenerParam param = new MqListenerParam();
            param.setMqListener(mqListener).setMqMode(mqMode).setBean(bean).setMethod(method.method);
            addMqListenerParam(mqMode, param);
        } else if (nameFlag) {
            // 2、设置消息业务名称
            MqConfigurationProperties properties = mqProperties.getMqs().get(name);
            Assert.isTrue(null != properties, "@MqListener attribute name is [" + name + "], not found in the configuration [mq.mqs." + name + "],[" + bean.getClass().getName() + "],[" + method.method.getName() + "]");
            topic = properties.getTopic();
            group = properties.getGroup();
            Assert.isTrue(StringUtils.hasText(topic), "Please specific [topic] under [mq.mqs." + name + "] configuration.");
            Assert.isTrue(StringUtils.hasText(group), "Please specific [group] under [mq.mqs." + name + "] configuration.");
            MqMode mqMode = properties.getBinder();
            if (null == mqMode) {
                mqMode = mqProperties.getBinder();
            }
            Assert.isTrue(null != mqMode, "Please specific [binder] under [mq.mqs." + name + "] configuration  or [binder] under [mq] configuration.");
            
            MqListenerParam param = new MqListenerParam();
            try {
                // 修改 @MqListene 属性topic、group
                InvocationHandler handler = Proxy.getInvocationHandler(mqListener);
                Map<String, Object> readField = (Map<String, Object>) FieldUtils.readField(handler, "memberValues", true);
                readField.put("topic", topic);
                readField.put("group", group);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.isTrue(false, e.getMessage());
            }
            param.setMqListener(mqListener).setMqMode(mqMode).setBean(bean).setMethod(method.method);
            addMqListenerParam(mqMode, param);
        } else {
            // @MqListener 中 topic 或 group 只设置一个
            Assert.isTrue(topicFlag == groupFlag, "Please specific [topic,group] under @MqListener,[" + bean.getClass().getName() + "],[" + method.method.getName() + "]");
            Assert.isTrue(nameFlag, "Please specific [name] under @MqListener,[" + bean.getClass().getName() + "],[" + method.method.getName() + "]");
        }
    }
    
    private void addMqListenerParam(MqMode mqMode, MqListenerParam listenerParam) {
        List<MqListenerParam> listeners = this.getListeners(mqMode);
        if (null == listeners) {
            listeners = new ArrayList<>();
            listenerParams.put(mqMode, listeners);
        }
        listeners.add(listenerParam);
    }
    
    public List<MqListenerParam> getListeners(MqMode mqMode) {
        return listenerParams.get(mqMode);
    }
    
    @Override
    public int getOrder() {
        return 0;
    }

    private Collection<MqListener> findListenerAnnotations(AnnotatedElement element) {
        return MergedAnnotations.from(element, SearchStrategy.TYPE_HIERARCHY).stream(MqListener.class).map(MergedAnnotation::synthesize)
                .collect(Collectors.toList());
    }
    
    private static class ListenerMethod {

        final Method method; // NOSONAR

        final MqListener[] mqListener; // NOSONAR

        ListenerMethod(Method method, MqListener[] mqListener) { // NOSONAR
            this.method = method;
            this.mqListener = mqListener;
        }

    }

}
