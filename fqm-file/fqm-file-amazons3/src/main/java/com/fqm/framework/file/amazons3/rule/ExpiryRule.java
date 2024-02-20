/*
 * @(#)ExpiryRule.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-file-amazons3
 * 创建日期 : 2024年2月18日
 * 修改历史 : 
 *     1. [2024年2月18日]创建文件 by 傅泉明
 */
package com.fqm.framework.file.amazons3.rule;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration.Rule;
import com.amazonaws.services.s3.model.lifecycle.LifecycleAndOperator;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilter;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilterPredicate;
import com.amazonaws.services.s3.model.lifecycle.LifecyclePrefixPredicate;
import com.amazonaws.services.s3.model.lifecycle.LifecycleTagPredicate;
import com.fqm.framework.common.core.util.JsonUtil;
import com.fqm.framework.file.amazons3.AmazonS3Service;
import com.fqm.framework.file.amazons3.tag.ExpiryTag;

/**
 * 文件过期策略
 * @version 
 * @author 傅泉明
 */
public class ExpiryRule {

    private static final String EXPRIY_RULE_ID = "expiry-rule";
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 新增文件过期策略 
     * @param service
     */
    public void addExpiryRule(AmazonS3Service service, int expiryDay) {
        String bucketName = service.getBucketName();
        List<Rule> rules = null;
        Rule expiryRule = null;
        // 1、获取桶的策略
        BucketLifecycleConfiguration config = service.getBucketLifecycleConfiguration(service.getBucketName());
        if (null == config) {
            // 新增策略
            config = new BucketLifecycleConfiguration();
            rules = new ArrayList<>(1);
            expiryRule = new Rule().withId(EXPRIY_RULE_ID);
            rules.add(expiryRule);
        } else {
            rules = config.getRules();
            // 查找策略
            for (Rule r : rules) {
                if (EXPRIY_RULE_ID.equals(r.getId())) {
                    // 存在策略ID，更新
                    expiryRule = r;
                    break;
                }
            }
            if (null == expiryRule) {
                // 新增策略
                expiryRule = new Rule().withId(EXPRIY_RULE_ID);
                rules.add(expiryRule);
            }
        }
        // 更新策略
        expiryRule.setExpirationInDays(expiryDay);
        expiryRule.setStatus(BucketLifecycleConfiguration.ENABLED);
        
        LifecycleFilter filter = new LifecycleFilter();
        LifecycleTagPredicate tagp = new LifecycleTagPredicate(ExpiryTag.build());
        // 过滤所有路径
        LifecyclePrefixPredicate pre = new LifecyclePrefixPredicate("");
        
        List<LifecycleFilterPredicate> list = new ArrayList<>(2);
        list.add(pre);
        list.add(tagp);
        filter.setPredicate(new LifecycleAndOperator(list));
        
        expiryRule.setFilter(filter);
        
        // 2、新增或更新策略ID为expriy-rule
        config.setRules(rules);
        service.setBucketLifecycleConfiguration(bucketName, config);
        String ruleStr = JsonUtil.toJsonStr(expiryRule);
        logger.info("--->>> update expiry rule:{}", ruleStr);
    }
    
}
