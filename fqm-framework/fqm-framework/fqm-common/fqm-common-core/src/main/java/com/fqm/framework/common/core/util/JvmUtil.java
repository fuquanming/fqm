package com.fqm.framework.common.core.util;

import java.lang.reflect.Field;

import sun.misc.Unsafe;
/**
 * Jvm工具类
 * 1、获取 Unsafe 实现 cas 功能
 * 
 * @version 
 * @author 傅泉明
 */
public class JvmUtil {

    /**
     * 获取 Unsafe 实现 cas 功能
     * 1、获取cas操作的值的内存偏移量
     * private volatile int value;//值
     * long valueOffset = unsafe.objectFieldOffset(XXX.class.getDeclaredField("value"))
     * 2、cas 操作
     * unsafe.compareAndSwapInt(this, valueOffset, 1, 2);
     * @return
     */
    public static Unsafe getUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
