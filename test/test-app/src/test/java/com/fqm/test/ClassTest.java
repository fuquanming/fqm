///*
// * @(#)ClassTest.java
// * 
// * Copyright (c) 2015, All Rights Reserved
// * 项目名称 : test-app
// * 创建日期 : 2022年12月22日
// * 修改历史 : 
// *     1. [2022年12月22日]创建文件 by 傅泉明
// */
//package com.fqm.test;
//
//import java.lang.reflect.Constructor;
//import java.util.function.BiConsumer;
//
//import org.springframework.kafka.core.KafkaOperations;
//import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
//import org.springframework.util.ClassUtils;
//import org.springframework.util.backoff.FixedBackOff;
//
//import cn.hutool.core.util.ClassLoaderUtil;
//
///**
// * 
// * @version 
// * @author 傅泉明
// */
//public class ClassTest {
//
//    public static void main(String[] args) {
//        try {
//            String className = "org.springframework.kafka.listener.SeekToCurrentErrorHandler";
////            String className = "org.springframework.kafka.listener.DefaultErrorHandler";
//            Class<?> errorHandlerClass = ClassUtils
//                    .forName(className, ClassLoaderUtil.getClassLoader());
//            Constructor<?> con = errorHandlerClass.getConstructor(
//                    BiConsumer.class, org.springframework.util.backoff.BackOff.class);
//            System.out.println(con.getName());
//            KafkaOperations<?, ?> template = null;
//            Object obj =  con.newInstance(new DeadLetterPublishingRecoverer(template), new FixedBackOff(10 * 1000L, 1L));
//            System.out.println(obj);
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    
//}
