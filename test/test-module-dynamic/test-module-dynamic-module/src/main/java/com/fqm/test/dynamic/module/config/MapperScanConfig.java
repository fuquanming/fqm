package com.fqm.test.dynamic.module.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@MapperScan("com.fqm.test.dynamic.module.dao")
@Configuration
public class MapperScanConfig {

}
