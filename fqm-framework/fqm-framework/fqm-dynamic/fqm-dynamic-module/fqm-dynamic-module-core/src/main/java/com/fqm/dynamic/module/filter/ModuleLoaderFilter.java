/*
 * @(#)ModuleLoaderFilter.java
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
 * 模块加载过滤器
 * @version 
 * @author 傅泉明
 */
public interface ModuleLoaderFilter {
    /**
     * 加载 
     * @param moduleClassLoader
     * @return  true：可以交给后面的过滤器，false：阻止后续过滤器
     */
    public boolean loader(ModuleClassLoader moduleClassLoader);
    
}
