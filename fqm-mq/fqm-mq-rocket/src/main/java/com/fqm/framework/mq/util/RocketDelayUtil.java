package com.fqm.framework.mq.util;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.fqm.framework.common.core.util.JsonUtil;

/**
 * RocketMq 延迟级别
 * 延迟时间存在十八个等级 (1s/5s/10s/30s/1m/2m/3m/4m/5m/6m/7m/8m/9m/10m/20m/30m/1h/2h )
 * 通过18级的时间，每次比较最近所在的级别时间投递进去。
 * 计算下次投递的级别
 * @version 
 * @author 傅泉明
 */
public class RocketDelayUtil {
    
    private RocketDelayUtil() {
    }
    
    /** 1s/5s/10s/30s */
    protected static final Map<Integer, Integer> SECOND_MAP = new TreeMap<>();

    /** 1m/2m/3m/4m/5m/6m/7m/8m/9m/10m/20m/30m */
    protected static final Map<Integer, Integer> MINUTE_MAP = new TreeMap<>();

    /** 1h/2h */
    protected static final Map<Integer, Integer> HOUR_MAP = new TreeMap<>();

    static {
        int sixty = 60;
        for (int i = 0; i < sixty; i++) {
            if (i <= 1) {
                SECOND_MAP.put(i, 1);
            } else if (i > 1 && i < 5) {
                SECOND_MAP.put(i, 1);
            } else if (i >= 5 && i < 10) {
                SECOND_MAP.put(i, 5);
            } else if (i >= 10 && i < 30) {
                SECOND_MAP.put(i, 10);
            } else if (i >= 30 && i < 60) {
                SECOND_MAP.put(i, 30);
            }
        }

        for (int i = 0; i < sixty; i++) {
            if (i <= 1) {
                MINUTE_MAP.put(i, 1);
            } else if (i > 1 && i < 2) {
                MINUTE_MAP.put(i, 1);
            } else if (i >= 2 && i < 3) {
                MINUTE_MAP.put(i, 2);
            } else if (i >= 3 && i < 4) {
                MINUTE_MAP.put(i, 3);
            } else if (i >= 4 && i < 5) {
                MINUTE_MAP.put(i, 4);
            } else if (i >= 5 && i < 6) {
                MINUTE_MAP.put(i, 5);
            } else if (i >= 6 && i < 7) {
                MINUTE_MAP.put(i, 6);
            } else if (i >= 7 && i < 8) {
                MINUTE_MAP.put(i, 7);
            } else if (i >= 8 && i < 9) {
                MINUTE_MAP.put(i, 8);
            } else if (i >= 9 && i < 10) {
                MINUTE_MAP.put(i, 9);
            } else if (i >= 10 && i < 20) {
                MINUTE_MAP.put(i, 10);
            } else if (i >= 20 && i < 30) {
                MINUTE_MAP.put(i, 20);
            } else if (i >= 30 && i < 60) {
                MINUTE_MAP.put(i, 30);
            }
        }
        int three = 3;
        for (int i = 0; i < three; i++) {
            if (i <= 1) {
                HOUR_MAP.put(i, 1);
            } else if (i > 1 && i < 2) {
                HOUR_MAP.put(i, 1);
            } else if (i >= 2) {
                HOUR_MAP.put(i, 2);
            }
        }
    }

    /** 1s/5s/10s/30s->1,2,3,4 */
    protected static final Map<Integer, Integer> SECOND_LEVEL_MAP = new TreeMap<>();

    /** 1m/2m/3m/4m/5m/6m/7m/8m/9m/10m/20m/30m->5,6,7,8,9,10,11,12,13,14,15,16 */
    protected static final Map<Integer, Integer> MINUTE_LEVEL_MAP = new TreeMap<>();

    /** 1h/2h->17,18 */
    protected static final Map<Integer, Integer> HOUR_LEVEL_MAP = new TreeMap<>();
    /** hourLevelMap 最后一个元素 */
    protected static final Integer HOUR_LEVEL_MAP_LAST;

    static {
        SECOND_LEVEL_MAP.put(1, 1);
        SECOND_LEVEL_MAP.put(5, 2);
        SECOND_LEVEL_MAP.put(10, 3);
        SECOND_LEVEL_MAP.put(30, 4);
        int step = 5;
        int ten = 10;
        for (int i = 1; i <= ten; i++) {
            MINUTE_LEVEL_MAP.put(i, step);
            step++;
        }
        MINUTE_LEVEL_MAP.put(20, 15);
        MINUTE_LEVEL_MAP.put(30, 16);

        HOUR_LEVEL_MAP.put(1, 17);
        HOUR_LEVEL_MAP.put(2, 18);
        
        HOUR_LEVEL_MAP_LAST = 2;
    }
    /**
     * 延迟时间转换为延迟级别
     * @param delayTime
     * @param timeUnit
     * @return
     */
    public static int getDelayLevel(int delayTime, TimeUnit timeUnit) {
        int minuteToSecond = 60;
        int hourToSecond = 3600;
        long second = timeUnit.toSeconds(delayTime);
        if (second < minuteToSecond) {
            Integer secondLevel = SECOND_MAP.get((int)second);
            return SECOND_LEVEL_MAP.get(secondLevel);
        } else if (second < hourToSecond) {
            long minute = timeUnit.toMinutes(delayTime);
            Integer minuteLevel = MINUTE_MAP.get((int)minute);
            return MINUTE_LEVEL_MAP.get(minuteLevel);
        } else {
            long hour = timeUnit.toHours(delayTime);
            Integer hourLevel = HOUR_MAP.get((int)hour);
            if (hourLevel == null) {
                // 取最后一个
                hourLevel = HOUR_LEVEL_MAP_LAST;
            }
            return HOUR_LEVEL_MAP.get(hourLevel);
        }
    }
    
    private static String delayInfoKey = "delayInfo";
    private static String timeUnitKey = "timeUnit";
    private static String delayTimeKey = "delayTime";
    private static String executeTimeKey = "executeTime";
    
    /**
     * 将消息添加上延迟时间
     * @param msg
     * @return
     */
    public static String buildDelayMsg(Object msg, int delayTime, TimeUnit timeUnit) {
        String jsonMsg = JsonUtil.toJsonStr(msg);
        Map<String, Object> valueMap = JsonUtil.toMap(jsonMsg);
        Map<String, Object> delayMap = new HashMap<>(3);
        valueMap.put(delayInfoKey, delayMap);
        delayMap.put(timeUnitKey, timeUnit.toString());
        delayMap.put(delayTimeKey, delayTime);
        delayMap.put(executeTimeKey, System.currentTimeMillis() + timeUnit.toMillis(delayTime));
        return JsonUtil.toJsonStr(valueMap);
    }
    /**
     * 移除延迟消息体
     * @param msgJson
     * @return
     */
    public static String buildRemoveDelayMsgJson(String msgJson) {
        Map<String, Object> msgMap = JsonUtil.toMap(msgJson);
        msgMap.remove(delayInfoKey);
        return JsonUtil.toJsonStr(msgMap);
    }
    
    /**
     * 解析json消息，计算下次延时时间(毫秒) 
     * @param msg
     * @return -1：不是延迟消息
     */
    @SuppressWarnings("unchecked")
    public static int getNextDelaySecond(String jsonMsg) {
        Map<String, Object> valueMap = JsonUtil.toMap(jsonMsg);
        Map<String, Object> delayInfoMap = (Map<String, Object>)valueMap.get(delayInfoKey);
        if (delayInfoMap != null) {
            long executeTime = Long.parseLong(delayInfoMap.get(executeTimeKey).toString());
            long now = System.currentTimeMillis();
            long delayTime = executeTime - now;
            if (delayTime <= 0) {
                return 0;
            }
            return (int) TimeUnit.MILLISECONDS.toMillis(delayTime);
        }
        return -1;
    }
}
