/*
 * @(#)JobMode.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-job-core
 * 创建日期 : 2022年9月2日
 * 修改历史 : 
 *     1. [2022年9月2日]创建文件 by 傅泉明
 */
package com.fqm.framework.job;

/**
 * 任务的方式
 * xxljob
 * elasticjob
 * @version 
 * @author 傅泉明
 */
public enum JobMode {
    /** xxljob */
    XXLJOB,
    /** elasticjob */
    ELASTICJOB;
    
    /**
     * 从字符串转化为 JobMode 
     * @param mode  任务模式的字符串
     * @return
     */
    public static JobMode getMode(String mode) {
        if (mode == null || "".equals(mode)) {
            return null;
        }
        JobMode[] modes = values();
        for (JobMode m : modes) {
            if (mode.toUpperCase().equals(m.name())) {
                return m;
            }
        }
        return null;
    }
    
    /**
     * 是否和字符串相等，不区分大小写
     * @param mode  任务模式的字符串
     * @return
     */
    public boolean equals(String mode) {
        return name().equalsIgnoreCase(mode);
    }
}
