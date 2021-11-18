package com.fqm.framework.common.mq.annotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.util.ReflectionUtils;

import com.fqm.framework.common.core.util.StringUtil;
import com.fqm.framework.common.mq.listener.MqListenerParam;
import com.fqm.framework.common.spring.util.ValueUtil;

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
                    String destination = mqListener.destination();// topic
                    String group = mqListener.group();// 分组
                    String binder = mqListener.binder();// mq
                    if (StringUtil.isNotEmpty(destination) &&
                            StringUtil.isNotEmpty(group) && StringUtil.isNotEmpty(binder)) {
                        
                        String destinationStr = ValueUtil.resolveExpression((ConfigurableBeanFactory)beanFactory, destination).toString();
                        String groupStr = ValueUtil.resolveExpression((ConfigurableBeanFactory)beanFactory, group).toString();
                        String binderStr = ValueUtil.resolveExpression((ConfigurableBeanFactory)beanFactory, binder).toString();
                        MqListenerParam param = new MqListenerParam();
                        param.setDestination(destinationStr);
                        param.setGroup(groupStr);
                        param.setBinder(binderStr);
                        param.setBean(bean);
                        param.setMethod(method.method);
                        param.setConcurrentConsumers(mqListener.concurrentConsumers() <= 0 ? 1 : mqListener.concurrentConsumers());
                        listeners.add(param);
                    }
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
        return MergedAnnotations.from(element, SearchStrategy.TYPE_HIERARCHY).stream(MqListener.class).map(ann -> ann.synthesize())
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
