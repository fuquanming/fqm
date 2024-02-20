/*
 * @(#)ExpiryUploadFilter.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-file-amazons3
 * 创建日期 : 2024年2月2日
 * 修改历史 : 
 *     1. [2024年2月2日]创建文件 by 傅泉明
 */
package com.fqm.framework.file.amazons3.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Tag;
import com.fqm.framework.file.amazons3.AmazonS3Service;
import com.fqm.framework.file.amazons3.tag.ExpiryTag;

/**
 * 设置对象过期的过滤器
 * @version 
 * @author 傅泉明
 */
public class ExpiryUploadFilter implements UploadFilter {
    
    /** 过滤器排序，从小到大执行 */
    private int order;
    private AmazonS3Service service;
    
    public ExpiryUploadFilter(int order, AmazonS3Service service) {
        this.order = order;
        this.service = service;
    }

    /**
     * @see com.fqm.framework.file.amazons3.filter.UploadFilter#getOrder()
     **/
    @Override
    public int getOrder() {
        return order;
    }

    /**
     * 上传对象是设置标签，用于生命周期策略命中
     * @see com.fqm.framework.file.amazons3.filter.UploadFilter#beforeUpload(com.amazonaws.services.s3.model.PutObjectRequest)
     *
     */
    @Override
    public boolean beforeUpload(PutObjectRequest request) {
        ObjectTagging tagging = request.getTagging();
        if (tagging == null) {
            tagging = new ObjectTagging(new ArrayList<>());
        } else {
            if (null == tagging.getTagSet()) {
                tagging.setTagSet(new ArrayList<>());
            }
        }
        List<Tag> tags = tagging.getTagSet();
        addExpriyTag(tags);
        
        request.setTagging(tagging);
        return true;
    }
    
    /**
     * 分片上传完成后设置该对象的标签，用于生命周期策略命中 
     * @see com.fqm.framework.file.amazons3.filter.UploadFilter#afterUpload(com.amazonaws.services.s3.model.CompleteMultipartUploadRequest)
     */
    @Override
    public void afterUpload(CompleteMultipartUploadRequest request) {
        // 1、获取标签
        String bucketName = request.getBucketName();
        String objectName = request.getKey();
        List<Tag> tags = service.getObjectTagging(bucketName, objectName);
        // 2、设置标签
        addExpriyTag(tags);
        service.setObjectTagging(bucketName, objectName, tags);
    }
    
    /**
     * 添加过期标签（移除过期标签重名的key，添加自定义的过期标签）
     * @param tags
     */
    private void addExpriyTag(List<Tag> tags) {
        Tag expriyTag = ExpiryTag.build();
        for (Iterator<Tag> it = tags.iterator(); it.hasNext();) {
            Tag tag = it.next();
            if (expriyTag.getKey().equals(tag.getKey())) {
                it.remove();
                break;
            }
        }
        tags.add(expriyTag);
    }
}
