package com.fqm.test.cache.controller;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.test.cache.service.UserService;

import cn.hutool.core.thread.ThreadUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
public class CacheController {
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    UserService userService;
    
    @ApiOperation("获取缓存用户")
    @GetMapping("/user/getCache")
    @ResponseBody
    public Object getCache(
            @ApiParam(name = "id", type = "Long", example = "1", required = false) 
            @RequestParam(name = "id", defaultValue = "100", required = false) Long id) {            
        ThreadUtil.concurrencyTest(5, new Runnable() {
            @Override
            public void run() {
                userService.getCacheById(id);
            }
        });        
        return userService.getCacheById(id);
    }
    
}
