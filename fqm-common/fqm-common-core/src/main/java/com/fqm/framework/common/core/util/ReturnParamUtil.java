/*
 * @(#)ReturnParamUtil.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-common-core
 * 创建日期 : 2021年7月1日
 * 修改历史 : 
 *     1. [2021年7月1日]创建文件 by 傅泉明
 */
package com.fqm.framework.common.core.util;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;

/**
 * 返回参数工具类，2个数据及3个数据
 * @version 
 * @author 傅泉明
 */
public class ReturnParamUtil {

    private ReturnParamUtil() {
    }
    /**
     * <p>创建两个推断泛型类型的对象的不可变对</p>
     *
     * @param <L> the left element type
     * @param <R> the right element type
     * @param left  the left element, may be null
     * @param right  the right element, may be null
     * @return a pair formed from the two parameters, not null
     */
    public static <L, R> ImmutablePair<L, R> of(final L left, final R right) {
        return new ImmutablePair<>(left, right);
    }

    /**
     * <p>创建三个推断泛型类型的对象的不可变三元组</p>
     *
     * <p>This factory allows the triple to be created using inference to
     * obtain the generic types.</p>
     *
     * @param <L> the left element type
     * @param <M> the middle element type
     * @param <R> the right element type
     * @param left  the left element, may be null
     * @param middle  the middle element, may be null
     * @param right  the right element, may be null
     * @return a triple formed from the three parameters, not null
     */
    public static <L, M, R> ImmutableTriple<L, M, R> of(final L left, final M middle, final R right) {
        return new ImmutableTriple<>(left, middle, right);
    }
    
}
