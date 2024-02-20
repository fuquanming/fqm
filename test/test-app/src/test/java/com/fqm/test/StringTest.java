//package com.fqm.test;
//
//import org.springframework.util.Assert;
//import org.springframework.util.StringUtils;
//
//import com.fqm.framework.locks.config.LockConfigurationProperties;
//
//public class StringTest {
//
//    public static void main(String[] args) {
////        String topic = "";
////        String group = "1";
////        boolean topicFlag = StringUtils.hasText(topic);
////        boolean groupFlag = StringUtils.hasText(group);
////        System.out.println(topicFlag);
////        System.out.println(groupFlag);
////        Assert.isTrue(topicFlag == groupFlag, "Please specific [topic,group] under @MqListener.");
//        
//        
//        String name = "";
//        String key = "";
//        boolean nameFlag = StringUtils.hasText(name);
//        boolean keyFlag = StringUtils.hasText(key);
//        if (nameFlag) {
//            LockConfigurationProperties properties = new LockConfigurationProperties();
//            Assert.notNull(properties, "@Lock4j attribute name is [" + name + "], not found in the configuration [lock.locks." + name + "]");
//        } else if (keyFlag) {
//        } else {
//            Assert.isTrue(false, "@Lock4j attribute name or key is required");
//        }
//    }
//
//}
