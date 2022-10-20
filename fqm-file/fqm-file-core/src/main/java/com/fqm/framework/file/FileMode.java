/*
 * @(#)FileMode.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-file-core
 * 创建日期 : 2022年9月12日
 * 修改历史 : 
 *     1. [2022年9月12日]创建文件 by 傅泉明
 */
package com.fqm.framework.file;

/**
 * 文件存储的方式，和文件模板一一对应
 * 
 * @version 
 * @author 傅泉明
 */
public enum FileMode {
    /** Minio存储 */
    minio;
    
    public static FileMode getMode(String mode) {
        if (mode == null || "".equals(mode)) {
            return null;
        }
        FileMode[] modes = values();
        for (FileMode m : modes) {
            if (mode.equals(m.name())) {
                return m;
            }
        }
        return null;
    }
    
}
