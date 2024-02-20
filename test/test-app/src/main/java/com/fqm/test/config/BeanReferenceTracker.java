package com.fqm.test.config;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.fqm.test.controller.DeptController;
import com.fqm.test.service.TestService;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
 
@Component
public class BeanReferenceTracker implements BeanFactoryPostProcessor,
    BeanPostProcessor {
 
    private final Map<String, String[]> beanReferences = new HashMap<>();
 
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            // 检查beanName是否引用了其他bean
            String[] dependsOn = beanFactory.getBeanDefinition(beanName).getDependsOn();
            if (dependsOn != null && dependsOn.length > 0) {
                beanReferences.put(beanName, dependsOn);
            }
        }
        // 现在beanReferences包含了所有的bean引用信息
    }
 
    // 提供方法来获取这些信息
    public Map<String, String[]> getBeanReferences() {
        return beanReferences;
    }
    
    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
//        final List<ListenerMethod> methods = new ArrayList<>();
//        ReflectionUtils.doWithMethods(targetClass, method -> {
//            Collection<Lock4j> listenerAnnotations = findListenerAnnotations(method);
//            if (!listenerAnnotations.isEmpty()) {
//                methods.add(new ListenerMethod(bean.getClass().getName(), method.getName(), listenerAnnotations.toArray(new Lock4j[listenerAnnotations.size()])));
//            }
//        }, ReflectionUtils.USER_DECLARED_METHODS);

//        ReflectionUtils.doWithFields(targetClass, field -> {
//            if (targetClass == DeptController.class) {
//                System.out.println("-------findClass---" + field.getType());
//                Object readField = FieldUtils.readField(field, bean);
//                if (AopUtils.getTargetClass(readField) == TestService.class) {
//                    System.out.println("-------find---" + field.getName());
//                }
//            }
//        }, ReflectionUtils.COPYABLE_FIELDS);
        
        return bean;
    }

//    private Collection<Lock4j> findListenerAnnotations(AnnotatedElement element) {
//        return MergedAnnotations.from(element, SearchStrategy.TYPE_HIERARCHY)
//                .stream(Lock4j.class).map(MergedAnnotation::synthesize)
//                .collect(Collectors.toList());
//    }
}
