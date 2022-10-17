package com.fqm.framework.common.core.util;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 使用Jackson
 * 
 * @version 
 * @author 傅泉明
 */
public class JsonUtil {

    private static ObjectMapper mapper;
    /** 设置通用属性 */
    static {
        mapper = new ObjectMapper();

        /** 如果json中有新增的字段并且是实体类类中不存在的，不报错 */
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        /** 如果存在未知属性，则忽略不报错 */
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        /** 允许key没有双引号 */
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        /** 允许key有单引号 */
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        /** 允许整数以0开头 */
//        mapper.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
        /** 允许字符串中存在回车换行控制符 */
//        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

    }

    /**
     * 转换为JSON字符串
     *
     * @param obj 被转为JSON的对象
     * @return JSON字符串
     */
    public static String toJsonStr(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * JSON字符串转为实体类对象
     *
     * @param <T>           Bean类型
     * @param jsonString    JSON字符串
     * @param clazz         实体类对象
     * @return 实体类对象
     */
    public static <T> T toBean(String jsonString, Class<T> clazz) {
        try {
            return mapper.readValue(jsonString, clazz);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * JSON字符串转为List对象
     *
     * @param jsonString    JSON字符串
     * @return List<T>
     */
    public static <T> List<T> toList(String jsonString, Class<T> clazz) {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, clazz);
        try {
            return mapper.readValue(jsonString, javaType);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * JSON字符串转为Map对象
     *
     * @param jsonString    JSON字符串
     * @return Map<String, Object>
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(String jsonString) {
        try {
            return mapper.readValue(jsonString, Map.class);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
