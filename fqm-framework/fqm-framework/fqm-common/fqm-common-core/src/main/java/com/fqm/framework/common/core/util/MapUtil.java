/*
 * @(#)MapUtil.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-common-core
 * 创建日期 : 2021年7月1日
 * 修改历史 : 
 *     1. [2021年7月1日]创建文件 by 傅泉明
 */
package com.fqm.framework.common.core.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBiMap;

/**
 * 
 * @version 
 * @author 傅泉明
 */
public class MapUtil {

    /**
     * 一个key可以映射多个value的HashMap
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> ArrayListMultimap<K, V> createListMap() {
        return ArrayListMultimap.create();
    }
    
    /**
     * 一种连value也不能重复的HashMap
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> HashBiMap<K, V> createUniqueValueMap() {
        return HashBiMap.create();
    }
    
}
