/*
 * @(#)UploadFilter.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-file-amazons3
 * 创建日期 : 2024年2月1日
 * 修改历史 : 
 *     1. [2024年2月1日]创建文件 by 傅泉明
 */
package com.fqm.framework.file.amazons3.filter;

import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * 上传过滤器
 * @version 
 * @author 傅泉明
 */
public interface UploadFilter extends Comparable<UploadFilter> {

    /**
     * 执行顺序，从小到大执行
     * @return
     */
    int getOrder();
    
    /**
     * 上传前处理上传对象
     * @param request   上传的对象
     * @return
     */
    default boolean beforeUpload(PutObjectRequest request) {
        return true;
    }
    
    /**
     * 上传分片前处理上传对象
     * @param request   上传的对象
     * @return
     */
    default boolean beforeUpload(InitiateMultipartUploadRequest request) {
        return true;
    }    
    
    /**
     * 上传后处理上传对象
     * @param request    上传后上传的对象
     * @return
     */
    default void afterUpload(PutObjectRequest request) {
    }
    
    /**
     * 上传后处理上传对象分片合并
     * @param request    上传后上传的对象分片合并
     * @return
     */
    default void afterUpload(CompleteMultipartUploadRequest request) {
    }

    /**
     * 按order从小到大排序 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @param o     需要比较的对象
     * @return
     */
    @Override
    default int compareTo(UploadFilter o) {
        if (this.getOrder() < o.getOrder()) {
            return -1;
        } else if (this.getOrder() > o.getOrder()) {
            return 1;
        } else {
            // order 一样，比较对象hashcode值
            if (this.hashCode() < o.hashCode()) {
                return -1;
            } else if (this.hashCode() > o.hashCode()) {
                return 1;
            }
            return 0;
        }
    }
}
