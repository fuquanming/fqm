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
    xxljob,
    /** elasticjob */
    elasticjob;
    
    public static JobMode getMode(String mode) {
        if (mode == null || "".equals(mode)) {
            return null;
        }
        JobMode[] modes = values();
        for (JobMode m : modes) {
            if (mode.equals(m.name())) {
                return m;
            }
        }
        return null;
    }
}
