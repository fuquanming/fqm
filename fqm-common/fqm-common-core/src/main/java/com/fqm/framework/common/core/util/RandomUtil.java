/*
 * @(#)RandomUtil.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-common-core
 * 创建日期 : 2021年3月19日
 * 修改历史 : 
 *     1. [2021年3月19日]创建文件 by 傅泉明
 */
package com.fqm.framework.common.core.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * 
 * @version 
 * @author 傅泉明
 */
public class RandomUtil {
    
    private static SecureRandom secureRandom = null;
    
    static {
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    
    private RandomUtil() {
    }
    
    /**
     * <p>
     * Returns a random integer within the specified range. within 0 - endExclusive
     * </p>
     *
     * @param startInclusive 0
     * @param endExclusive
     *            the upper bound (not included)
     * @throws IllegalArgumentException
     *             if {@code startInclusive > endExclusive} or if
     *             {@code startInclusive} is negative
     * @return the random integer
     */
    public static int nextInt(final int endExclusive) {
        return secureRandom.nextInt(endExclusive);
    }

    /**
     * <p> Returns a random int within 0 - Integer.MAX_VALUE </p>
     *
     * @return the random integer
     * @see #nextInt(int, int)
     * @since 3.5
     */
    public static int nextInt() {
        return secureRandom.nextInt(Integer.MAX_VALUE);
    }

    /**
     * <p> Returns a random long within 0 - Long.MAX_VALUE </p>
     *
     * @return the random long
     * @see #nextLong(long, long)
     * @since 3.5
     */
    public static long nextLong() {
        return secureRandom.nextLong();
    }
    
}
