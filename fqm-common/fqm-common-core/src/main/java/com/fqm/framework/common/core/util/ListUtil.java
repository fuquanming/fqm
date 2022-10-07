/*
 * @(#)ListUtil.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-common-core
 * 创建日期 : 2021年7月1日
 * 修改历史 : 
 *     1. [2021年7月1日]创建文件 by 傅泉明
 */
package com.fqm.framework.common.core.util;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * 
 * @version 
 * @author 傅泉明
 */
public class ListUtil {

    /**
     * List切割
     * @param <T>
     * @param list
     * @param size
     * @return
     */
    public static <T> List<List<T>> partition(List<T> list, int size) {
        return Lists.partition(list, size);
    }
}
