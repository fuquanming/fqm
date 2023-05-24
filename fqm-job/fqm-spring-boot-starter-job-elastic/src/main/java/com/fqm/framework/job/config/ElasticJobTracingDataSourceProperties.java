/*
 * @(#)ElasticJobTracingDataSourceProperties.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-job-elastic
 * 创建日期 : 2023年5月23日
 * 修改历史 : 
 *     1. [2023年5月23日]创建文件 by 傅泉明
 */
package com.fqm.framework.job.config;

/**
 * ElasticJob 事务跟踪的数据库连接配置
 * @version 
 * @author 傅泉明
 */
public class ElasticJobTracingDataSourceProperties {
    
    /** 事务跟踪类型 */
    public static final String TRACING_TYPE = "RDB";
    
    /** 数据库配置 */
    private Db db;
    
    public Db getDb() {
        return db;
    }

    public void setDb(Db db) {
        this.db = db;
    }

    public static class Db {
        /**
         * Fully qualified name of the JDBC driver. Auto-detected based on the URL by default.
         */
        private String driverClassName;
        
        /**
         * JDBC URL of the database.
         */
        private String url;
        
        /**
         * Login username of the database.
         */
        private String username;
        
        /**
         * Login password of the database.
         */
        private String password;
        
        public String getDriverClassName() {
            return driverClassName;
        }
        
        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
    
}
