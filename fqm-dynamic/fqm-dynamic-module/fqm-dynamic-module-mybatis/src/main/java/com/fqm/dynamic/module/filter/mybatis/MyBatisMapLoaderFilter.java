/*
 * @(#)MyBatisMapLoader.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-dynamic-module-mybatis
 * 创建日期 : 2022年6月21日
 * 修改历史 : 
 *     1. [2022年6月21日]创建文件 by 傅泉明
 */
package com.fqm.dynamic.module.filter.mybatis;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.dynamic.module.core.ModuleClassLoader;
import com.fqm.dynamic.module.filter.ModuleLoaderFilter;

/**
 * mybatis的mapper.xml和@Mapper加载类
 * @version 
 * @author 傅泉明
 */
public class MyBatisMapLoaderFilter implements ModuleLoaderFilter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /** 存储mapper.xml的文件夹 */
    private String mapperXmlFolder = "mapper/";

    /** 存储mapper.xml的文件夹 */
    private String mapperXmlSuffix = ".xml";

    private SqlSessionFactory sqlSessionFactory;

    public MyBatisMapLoaderFilter(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }
    /**
     * 
     * @param mapperXmlFolder   xml文件所在文件路径包含/，如:mapper/
     * @param sqlSessionFactory
     */
    public MyBatisMapLoaderFilter(String mapperXmlFolder, SqlSessionFactory sqlSessionFactory) {
        if (mapperXmlFolder != null) {
            this.mapperXmlFolder = mapperXmlFolder;
        }
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public boolean loader(ModuleClassLoader moduleClassLoader) {
        Map<String, byte[]> fileByteMap = moduleClassLoader.getFileMap();
        for (Entry<String, byte[]> entry : fileByteMap.entrySet()) {
            String resource = entry.getKey();
            if (resource.startsWith(mapperXmlFolder) && resource.endsWith(mapperXmlSuffix)) {
                byte[] bytes = entry.getValue();
                /** 加载并解析对应xml */
                XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(new ByteArrayInputStream(bytes), sqlSessionFactory.getConfiguration(),
                        resource, sqlSessionFactory.getConfiguration().getSqlFragments());
                xmlMapperBuilder.parse();
                logger.info("loader MyBatisMap={}", resource);
            }
        }
        this.sqlSessionFactory = null;
        return true;
    }

}
