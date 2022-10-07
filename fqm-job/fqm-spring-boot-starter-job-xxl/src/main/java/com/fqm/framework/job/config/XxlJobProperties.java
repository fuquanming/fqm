/*
 * @(#)XxlJobConfig.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-job-xxl
 * 创建日期 : 2022年9月2日
 * 修改历史 : 
 *     1. [2022年9月2日]创建文件 by 傅泉明
 */
package com.fqm.framework.job.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * xxl-job 配置类
 * @version 
 * @author 傅泉明
 */
@ConfigurationProperties("xxljob")
public class XxlJobProperties {
    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 控制器配置
     */
    private AdminProperties admin;

    /**
     * 执行器配置
     */
    private ExecutorProperties executor;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        if (accessToken != null && accessToken.trim().length() > 0) {
            this.accessToken = accessToken;
        }
    }

    public AdminProperties getAdmin() {
        return admin;
    }

    public void setAdmin(AdminProperties admin) {
        this.admin = admin;
    }

    public ExecutorProperties getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorProperties executor) {
        this.executor = executor;
    }

    /**
     * XXL-Job 调度器配置类
     */
    public static class AdminProperties {
        /**
         * 调度器地址
         */
        private String addresses;

        public String getAddresses() {
            return addresses;
        }

        public void setAddresses(String addresses) {
            this.addresses = addresses;
        }

        @Override
        public String toString() {
            return "AdminProperties{" + "addresses='" + addresses + '\'' + '}';
        }

    }

    /**
     * XXL-Job 执行器配置类
     */
    public static class ExecutorProperties {
        /**
         * 应用名
         */
        private String appName;

        /**
         * 执行器的 IP
         */
        private String ip;

        /**
         * 执行器的 Port,-1 表示随机
         */
        private Integer port = -1;

        /**
         * 日志地址
         */
        private String logPath;

        /**
         * 日志保留天数，-1，不清理，永久保留
         */
        private Integer logRetentionDays = -1;

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getLogPath() {
            return logPath;
        }

        public void setLogPath(String logPath) {
            this.logPath = logPath;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Integer getLogRetentionDays() {
            return logRetentionDays;
        }

        public void setLogRetentionDays(Integer logRetentionDays) {
            this.logRetentionDays = logRetentionDays;
        }
    }
}
