package com.fqm.framework.common.redis.listener.spring;

import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.Topic;

/**
 * Redis 监听主题管理器
 * @version 
 * @author 傅泉明
 */
public class TopicManager {
    
    /**
     * 所有主题
     * @return
     */
    public static Topic getAllTopic() {
        return new PatternTopic("__keyevent@*");
    }
    
    /**
     * 过期主题
     * @return
     */
    public static Topic getExpiredTopic() {
        return new PatternTopic("__keyevent@*__:expired");
    }
    
    /**
     * 新增、修改主题
     * @return
     */
    public static Topic getUpdateTopic() {
        return new PatternTopic("__keyevent@*__:set");
    }
    
    /**
     * 删除主题
     * @return
     */
    public static Topic getDeleteTopic() {
        return new PatternTopic("__keyevent@*__:del");
    }
    
}
