package com.fqm.framework.mq.constant;
/**
 * 常量
 * 
 * @version 
 * @author 傅泉明
 */
public class Constants {
    
    private Constants() {
    }
    /** Lua执行成功标志 */
    public static final String EXECUTE_SUCCESS = "OK";
    
    /** RedisTemplate 消息队列最大值 */
    public static final int MAX_QUEUE_SIZE = 1000;
    
    /** RedisTemplate 延迟消息队列TTL key的前缀 */
    public static final String DELAY_MESSAGE_TTL_PREFIX_KEY = "DM-TTL-";
    /** RedisTemplate 延迟消息队列HashMap key的前缀 */
    public static final String DELAY_MESSAGE_HASHMAP_PREFIX_KEY = "DM-";
    
    /** RedisTemplate 延迟消息队列消息中添加自定义消息字段 key的前缀 */
    public static final String DELAY_MESSAGE_FIELD_INFO = "delayMessageInfo";
    /** RedisTemplate 延迟消息队列消息中添加自定义过期时间字段 */
    public static final String DELAY_MESSAGE_FIELD_TIME = "time";
    /** RedisTemplate 延迟消息队列消息中添加自定义id字段 */
    public static final String DELAY_MESSAGE_FIELD_ID = "id";
    
    /** RedisTemplate 延迟消息队列TTL后投递消息次数 key的前缀 */
    public static final String DELAY_MESSAGE_INCR_PREFIX_KEY = "DM-INCR-";
}
