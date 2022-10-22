package com.fqm.framework.mq.annotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.util.ReflectionUtils;

import com.fqm.framework.common.spring.util.ValueUtil;
import com.fqm.framework.mq.listener.MqListenerParam;
import com.google.common.base.Preconditions;

/**
 * @MqListener 注解监听，并转换为 List<MqListenerParam> 对象
 * 参考 RabbitListenerAnnotationBeanPostProcessor
 * 
 * @version 
 * @author 傅泉明
 */
public class MqListenerAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered, BeanFactoryAware {

    private BeanFactory beanFactory;
    private List<MqListenerParam> listeners = new ArrayList<>();
    
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
                for (MqListener mqListener : method.annotations) {
                    // 消息名称
                    String name = mqListener.name();
                    String nameStr = ValueUtil.resolveExpression((ConfigurableBeanFactory)beanFactory, name).toString();
                    Preconditions.checkArgument(StringUtils.isNotBlank(nameStr), "Please specific [name] under mq configuration.");
                    MqListenerParam param = new MqListenerParam();
                    param.setName(nameStr).setBean(bean).setMethod(method.method);
                    listeners.add(param);
                }
            }
        }

        return bean;
    }
    
    public List<MqListenerParam> getListeners() {
        return listeners;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
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

        final MqListener[] annotations; // NOSONAR

        ListenerMethod(Method method, MqListener[] annotations) { // NOSONAR
            this.method = method;
            this.annotations = annotations;
        }

    }

}
