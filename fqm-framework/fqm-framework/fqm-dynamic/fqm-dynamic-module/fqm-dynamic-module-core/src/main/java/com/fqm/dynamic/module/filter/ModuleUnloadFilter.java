/*
 * @(#)ModuleUnloadFilter.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-dynamic-module-core
 * 创建日期 : 2022年6月21日
 * 修改历史 : 
 *     1. [2022年6月21日]创建文件 by 傅泉明
 */
package com.fqm.dynamic.module.filter;

import com.fqm.dynamic.module.core.ModuleClassLoader;

/**
 * 卸载模块
 * @version 
 * @author 傅泉明
 */
public interface ModuleUnloadFilter {
    /**
     * 卸载 
     * @param moduleClassLoader
     */
    public void unload(ModuleClassLoader moduleClassLoader);
}
