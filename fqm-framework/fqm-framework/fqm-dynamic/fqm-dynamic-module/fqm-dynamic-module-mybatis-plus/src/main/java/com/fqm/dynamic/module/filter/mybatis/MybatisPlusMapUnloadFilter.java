package com.fqm.dynamic.module.filter.mybatis;

import java.util.Map.Entry;
import java.util.Set;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.fqm.dynamic.module.filter.mybatis.MybatisMapUnloadFilter;
/**
 * MyBatisPlus 卸载过滤器
 * 
 * @version 
 * @author 傅泉明
 */
public class MybatisPlusMapUnloadFilter extends MybatisMapUnloadFilter {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    public MybatisPlusMapUnloadFilter(SqlSessionFactory sqlSessionFactory) {
        super(sqlSessionFactory);
    }
    /**
     * 
     * @param mapperXmlFolder   xml文件所在文件路径包含/，如:mapper/
     * @param sqlSessionFactory
     */
    public MybatisPlusMapUnloadFilter(String mapperXmlFolder, SqlSessionFactory sqlSessionFactory) {
        super(sqlSessionFactory);
    }
    
    @Override
    public String resourceFilter(Configuration configuration, Set<?> loadedResourcesSet, Entry<String, byte[]> entry)
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        String namespace = super.resourceFilter(configuration, loadedResourcesSet, entry);
        if (namespace != null) {
            /** mybatis-plus必须清除缓存 */
            Set<String> mapperRegistryCache = GlobalConfigUtils.getMapperRegistryCache(configuration);
            mapperRegistryCache.remove("interface " + namespace);
            logger.info("unload->interface=" + namespace);
        }
        return namespace;
    }
}
