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
    /** 1s/5s/10s/30s */
    public static Map<Integer, Integer> secondMap = new TreeMap<>();

    /** 1m/2m/3m/4m/5m/6m/7m/8m/9m/10m/20m/30m */
    public static Map<Integer, Integer> minuteMap = new TreeMap<>();

    /** 1h/2h */
    public static Map<Integer, Integer> hourMap = new TreeMap<>();

    static {
        int sixty = 60;
        for (int i = 0; i < sixty; i++) {
            if (i <= 1) {
                secondMap.put(i, 1);
            } else if (i > 1 && i < 5) {
                secondMap.put(i, 1);
            } else if (i >= 5 && i < 10) {
                secondMap.put(i, 5);
            } else if (i >= 10 && i < 30) {
                secondMap.put(i, 10);
            } else if (i >= 30 && i < 60) {
                secondMap.put(i, 30);
            }
        }

        for (int i = 0; i < sixty; i++) {
            if (i <= 1) {
                minuteMap.put(i, 1);
            } else if (i > 1 && i < 2) {
                minuteMap.put(i, 1);
            } else if (i >= 2 && i < 3) {
                minuteMap.put(i, 2);
            } else if (i >= 3 && i < 4) {
                minuteMap.put(i, 3);
            } else if (i >= 4 && i < 5) {
                minuteMap.put(i, 4);
            } else if (i >= 5 && i < 6) {
                minuteMap.put(i, 5);
            } else if (i >= 6 && i < 7) {
                minuteMap.put(i, 6);
            } else if (i >= 7 && i < 8) {
                minuteMap.put(i, 7);
            } else if (i >= 8 && i < 9) {
                minuteMap.put(i, 8);
            } else if (i >= 9 && i < 10) {
                minuteMap.put(i, 9);
            } else if (i >= 10 && i < 20) {
                minuteMap.put(i, 10);
            } else if (i >= 20 && i < 30) {
                minuteMap.put(i, 20);
            } else if (i >= 30 && i < 60) {
                minuteMap.put(i, 30);
            }
        }
        int three = 3;
        for (int i = 0; i < three; i++) {
            if (i <= 1) {
                hourMap.put(i, 1);
            } else if (i > 1 && i < 2) {
                hourMap.put(i, 1);
            } else if (i >= 2) {
                hourMap.put(i, 2);
            }
        }
    }

    /** 1s/5s/10s/30s->1,2,3,4 */
    public static Map<Integer, Integer> secondLevelMap = new TreeMap<>();

    /** 1m/2m/3m/4m/5m/6m/7m/8m/9m/10m/20m/30m->5,6,7,8,9,10,11,12,13,14,15,16 */
    public static Map<Integer, Integer> minuteLevelMap = new TreeMap<>();

    /** 1h/2h->17,18 */
    public static Map<Integer, Integer> hourLevelMap = new TreeMap<>();
    /** hourLevelMap 最后一个元素 */
    public static Integer hourLevelMapLast;

    static {
        secondLevelMap.put(1, 1);
        secondLevelMap.put(5, 2);
        secondLevelMap.put(10, 3);
        secondLevelMap.put(30, 4);
        int step = 5;
        int ten = 10;
        for (int i = 1; i <= ten; i++) {
            minuteLevelMap.put(i, step);
            step++;
        }
        minuteLevelMap.put(20, 15);
        minuteLevelMap.put(30, 16);

        hourLevelMap.put(1, 17);
        hourLevelMap.put(2, 18);
        
        hourLevelMapLast = 2;
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
            Integer secondLevel = secondMap.get((int)second);
//            System.out.println("second=" + secondLevel);
            return secondLevelMap.get(secondLevel);
        } else if (second < hourToSecond) {
            long minute = timeUnit.toMinutes(delayTime);
            Integer minuteLevel = minuteMap.get((int)minute);
//            System.out.println("minute=" + minuteLevel);
            return minuteLevelMap.get(minuteLevel);
        } else {
            long hour = timeUnit.toHours(delayTime);
            Integer hourLevel = hourMap.get((int)hour);
            if (hourLevel == null) {
                // 取最后一个
                hourLevel = hourLevelMapLast;
            }
//            System.out.println("hour=" + hourLevel);
            return hourLevelMap.get(hourLevel);
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
            long executeTime = Long.valueOf(delayInfoMap.get(executeTimeKey).toString());
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
