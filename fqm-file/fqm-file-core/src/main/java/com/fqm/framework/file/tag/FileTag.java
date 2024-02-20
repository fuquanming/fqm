/*
 * @(#)FileTag.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-file-core
 * 创建日期 : 2024年2月6日
 * 修改历史 : 
 *     1. [2024年2月6日]创建文件 by 傅泉明
 */
package com.fqm.framework.file.tag;

import java.io.Serializable;

/**
 * 文件标签
 * @version 
 * @author 傅泉明
 */
public class FileTag implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;

    private String value;

    public FileTag(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public FileTag withKey(String key) {
        setKey(key);
        return this;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public FileTag withValue(String value) {
        setValue(value);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FileTag tag = (FileTag) o;

        if (key != null ? !key.equals(tag.key) : tag.key != null) {
            return false;
        }
        return value != null ? value.equals(tag.value) : tag.value == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

}
