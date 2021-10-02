/*
 * @(#)CollectionUtil.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-common-core
 * 创建日期 : 2021年7月1日
 * 修改历史 : 
 *     1. [2021年7月1日]创建文件 by 傅泉明
 */
package com.fqm.framework.common.core.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

/**
 * 集合工具类
 * @version 
 * @author 傅泉明
 */
public class CollectionUtil {

    public static boolean isEmpty(final Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }
    
    public static boolean isNotEmpty(final Collection<?> coll) {
        return !isEmpty(coll);
    }
    /**
     * 两个集合取交集
     * @param <C>
     * @param collection
     * @param retain
     * @return
     */
    public static <C> Collection<C> retainAll(final Collection<C> collection, final Collection<?> retain) {
        return CollectionUtils.retainAll(collection, retain);
    }

    /**
     * 两个集合取并集
     * @param <O>
     * @param a
     * @param b
     * @return
     */
    public static <O> Collection<O> union(final Iterable<? extends O> a, final Iterable<? extends O> b) {
        return CollectionUtils.union(a, b);
    }
    
    /**
     * 两个集合取差集
     * @param <O>
     * @param a
     * @param b
     * @return
     */
    public static <O> Collection<O> subtract(final Iterable<? extends O> a, final Iterable<? extends O> b) {
        return CollectionUtils.subtract(a, b);
    }
    
    /************** lambda **************/
    /**
     * 执行List中对象的一个方法，并返回该方法结果，封装成List
     * @param <T>   List<User>
     * @param <U>   User::getId
     * @param from
     * @param func
     * @return
     */
    public static <T, U> List<U> convertList(List<T> from, Function<T, U> func) {
        return from.stream().map(func).collect(Collectors.toList());
    }

    /**
     * 执行List中对象的一个方法，并返回该方法结果，封装成Set
     * @param <T>   List<User>
     * @param <U>   User::getId
     * @param from
     * @param func
     * @return
     */
    public static <T, U> Set<U> convertSet(List<T> from, Function<T, U> func) {
        return from.stream().map(func).collect(Collectors.toSet());
    }

    /**
     * 执行List中对象的一个方法，封装成Map（key为方法的返回值，value为该对象）
     * @param <T>   List<User>
     * @param <U>   User::getId
     * @param from
     * @param func
     * @return
     */
    public static <T, K> Map<K, T> convertMap(List<T> from, Function<T, K> keyFunc) {
        return from.stream().collect(Collectors.toMap(keyFunc, item -> item));
    }

    /**
     * 执行List中对象的两个方法，封装成Map（key为keyFunc的返回值，value为valueFunc的返回值）
     * @param <T>   List<User>
     * @param <K>   User::getId
     * @param <V>   User::getName
     * @param from
     * @param keyFunc
     * @param valueFunc
     * @return
     */
    public static <T, K, V> Map<K, V> convertMap(List<T> from, Function<T, K> keyFunc, Function<T, V> valueFunc) {
        return from.stream().collect(Collectors.toMap(keyFunc, valueFunc));
    }

    /**
     * 执行List中对象的一个方法，封装成Map（key为方法的返回值，value为该对象有相同的key值的List）
     * @param <T>   List<User>
     * @param <K>   User::getId
     * @param from
     * @param keyFunc
     * @return
     */
    public static <T, K> Map<K, List<T>> convertMultiMap(List<T> from, Function<T, K> keyFunc) {
        return from.stream().collect(Collectors.groupingBy(keyFunc, Collectors.mapping(t -> t, Collectors.toList())));
    }

    /**
     * 执行List中对象的两个方法，封装成Map（key为keyFunc的返回值，value为valueFunc的返回值组成的List）
     * @param <T>   List<User>
     * @param <K>   User::getId
     * @param <V>   User::getName
     * @param from
     * @param keyFunc
     * @param valueFunc
     * @return
     */
    public static <T, K, V> Map<K, List<V>> convertMultiMap(List<T> from, Function<T, K> keyFunc, Function<T, V> valueFunc) {
        return from.stream().collect(Collectors.groupingBy(keyFunc, Collectors.mapping(valueFunc, Collectors.toList())));
    }

    /**
     * 执行List中对象的两个方法，封装成Map（key为keyFunc的返回值，value为valueFunc的返回值组成的Set）
     * @param <T>   List<User>
     * @param <K>   User::getId
     * @param <V>   User::getName
     * @param from
     * @param keyFunc
     * @param valueFunc
     * @return
     */
    public static <T, K, V> Map<K, Set<V>> convertMultiMapToSet(List<T> from, Function<T, K> keyFunc, Function<T, V> valueFunc) {
        return from.stream().collect(Collectors.groupingBy(keyFunc, Collectors.mapping(valueFunc, Collectors.toSet())));
    }

    public static <T> T getFirst(List<T> from) {
        return !isEmpty(from) ? from.get(0) : null;
    }
}
