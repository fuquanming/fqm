/*
 * @(#)MybatisMapUnloadFilter.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-dynamic-module-mybatis
 * 创建日期 : 2022年6月21日
 * 修改历史 : 
 *     1. [2022年6月21日]创建文件 by 傅泉明
 */
package com.fqm.dynamic.mybatis;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.binding.MapperProxyFactory;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.fqm.dynamic.module.core.ModuleClassLoader;
import com.fqm.dynamic.module.filter.ModuleUnloadFilter;

/**
 * 
 * @version 
 * @author 傅泉明
 */
public class MybatisMapUnloadFilter implements ModuleUnloadFilter {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    /** 存储mapper.xml的文件夹 */
    private String mapperXmlFolder = "mapper/";

    /** 存储mapper.xml的文件夹 */
    private String mapperXmlSuffix = ".xml";

    private SqlSessionFactory sqlSessionFactory;

    public MybatisMapUnloadFilter(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public void unload(ModuleClassLoader moduleClassLoader) {
        Map<String, byte[]> fileByteMap = moduleClassLoader.getFileMap();
        try {
            Configuration configuration = sqlSessionFactory.getConfiguration();
            /** 获取Configuration中的loadedResource,mybatis-plus和mybatis，mybatis-plus的Configuration是继承自mybatis的子类 */
            Set<?> loadedResourcesSet = (Set<?>) FieldUtils.readField(configuration, "loadedResources", true);
            for (Entry<String, byte[]> entry : fileByteMap.entrySet()) {
                String resource = entry.getKey();
                if (resource.startsWith(mapperXmlFolder) && resource.endsWith(mapperXmlSuffix)) {
                    byte[] bytes = entry.getValue();

                    /** 加载mybatis中的xml */
                    XPathParser xPathParser = new XPathParser(new ByteArrayInputStream(bytes), true, configuration.getVariables(),
                            new XMLMapperEntityResolver());
                    /** 解析mybatis的xml的根节点 */
                    XNode context = xPathParser.evalNode("/mapper");

                    /** 拿到namespace，namespace就是指Mapper接口的全限定名 */
                    String namespace = context.getStringAttribute("namespace");

                    Field knownMappersField = configuration.getMapperRegistry().getClass().getDeclaredField("knownMappers");
                    knownMappersField.setAccessible(true);

                    /** 存放Mapper接口和对应代理子类的映射map */
//                    Map<Class<?>, MapperProxyFactory<?>> mapConfig = 
//                            (Map<Class<?>, MapperProxyFactory<?>>) knownMappersField.get(configuration.getMapperRegistry());
                    Map<?, ?> mapConfig = (Map<?, ?>) knownMappersField.get(configuration.getMapperRegistry());
                    /** Mapper接口对应的class对象 */
                    Class<?> clazz = Resources.classForName(namespace);

                    /** 删除各种缓存 */
                    mapConfig.remove(clazz);
                    loadedResourcesSet.remove(resource);
                    loadedResourcesSet.remove(clazz.toString());
                    loadedResourcesSet.remove("namespace:" + clazz.getName());
                    configuration.getCacheNames().remove(namespace);

                    /** mybatis-plus必须清除缓存 */
                    Set<String> mapperRegistryCache = GlobalConfigUtils.getMapperRegistryCache(configuration);
                    mapperRegistryCache.remove("interface " + namespace);

                    /** 缓存的CRUD等 */
                    Collection<MappedStatement> mappedStatements = configuration.getMappedStatements();
                    List<MappedStatement> objects = new ArrayList<>();
                    Iterator<MappedStatement> it = mappedStatements.iterator();
                    while (it.hasNext()) {
                        Object object = it.next();
                        if (object instanceof MappedStatement) {
                            MappedStatement mappedStatement = (MappedStatement) object;
                            if (mappedStatement.getId().startsWith(namespace)) {
                                objects.add(mappedStatement);
                            }
                        }
                    }
                    mappedStatements.removeAll(objects);

                    /** 清掉namespace下各种缓存 */
                    cleanParameterMap(configuration, context.evalNodes("/mapper/parameterMap"), namespace);
                    cleanResultMap(configuration, context.evalNodes("/mapper/resultMap"), namespace);
                    cleanKeyGenerators(configuration, context.evalNodes("insert|update|select|delete"), namespace);
                    cleanSqlElement(configuration, context.evalNodes("/mapper/sql"), namespace);

                    logger.info("unload->resource=" + resource);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.sqlSessionFactory = null;
    }

    /**
     * 清理parameterMap
     *
     * @param list
     * @param namespace
     */
    private void cleanParameterMap(Configuration configuration, List<XNode> list, String namespace) {
        for (XNode parameterMapNode : list) {
            String id = parameterMapNode.getStringAttribute("id");
            configuration.getParameterMaps().remove(namespace + "." + id);
        }
    }

    /**
     * 清理resultMap
     *
     * @param list
     * @param namespace
     */
    private void cleanResultMap(Configuration configuration, List<XNode> list, String namespace) {
        for (XNode resultMapNode : list) {
            String id = resultMapNode.getStringAttribute("id", resultMapNode.getValueBasedIdentifier());
            configuration.getResultMapNames().remove(id);
            configuration.getResultMapNames().remove(namespace + "." + id);
            clearResultMap(configuration, resultMapNode, namespace);
        }
    }

    private void clearResultMap(Configuration configuration, XNode xNode, String namespace) {
        for (XNode resultChild : xNode.getChildren()) {
            if ("association".equals(resultChild.getName()) || "collection".equals(resultChild.getName()) || "case".equals(resultChild.getName())) {
                if (resultChild.getStringAttribute("select") == null) {
                    configuration.getResultMapNames().remove(resultChild.getStringAttribute("id", resultChild.getValueBasedIdentifier()));
                    configuration.getResultMapNames()
                            .remove(namespace + "." + resultChild.getStringAttribute("id", resultChild.getValueBasedIdentifier()));
                    if (resultChild.getChildren() != null && !resultChild.getChildren().isEmpty()) {
                        clearResultMap(configuration, resultChild, namespace);
                    }
                }
            }
        }
    }

    /**
     * 清理selectKey
     *
     * @param list
     * @param namespace
     */
    private void cleanKeyGenerators(Configuration configuration, List<XNode> list, String namespace) {
        for (XNode context : list) {
            String id = context.getStringAttribute("id");
            configuration.getKeyGeneratorNames().remove(id + SelectKeyGenerator.SELECT_KEY_SUFFIX);
            configuration.getKeyGeneratorNames().remove(namespace + "." + id + SelectKeyGenerator.SELECT_KEY_SUFFIX);

            Collection<MappedStatement> mappedStatements = configuration.getMappedStatements();
            List<MappedStatement> objects = new ArrayList<>();
            Iterator<MappedStatement> it = mappedStatements.iterator();
            while (it.hasNext()) {
                Object object = it.next();
                if (object instanceof MappedStatement) {
                    MappedStatement mappedStatement = (MappedStatement) object;
                    if (mappedStatement.getId().equals(namespace + "." + id)) {
                        objects.add(mappedStatement);
                    }
                }
            }
            mappedStatements.removeAll(objects);
        }
    }

    /**
     * 清理sql节点缓存
     *
     * @param list
     * @param namespace
     */
    private void cleanSqlElement(Configuration configuration, List<XNode> list, String namespace) {
        for (XNode context : list) {
            String id = context.getStringAttribute("id");
            configuration.getSqlFragments().remove(id);
            configuration.getSqlFragments().remove(namespace + "." + id);
        }
    }
}
