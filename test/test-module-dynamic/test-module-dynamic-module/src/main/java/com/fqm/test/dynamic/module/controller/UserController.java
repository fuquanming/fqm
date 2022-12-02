package com.fqm.test.dynamic.module.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.common.core.util.IdUtil;
import com.fqm.framework.common.core.vo.Result;
import com.fqm.test.dynamic.module.service.UserService;
import com.fqm.test.model.User;

import io.swagger.annotations.ApiOperation;

@RestController
public class UserController {

    @Resource
    UserService userService;
    
    @ApiOperation("新增用户")
    @GetMapping("/user/insert")
    public Result<User> insertUser() {
        User user = new User();
        user.setId(IdUtil.getSnowflake().nextId());
        userService.insert(user);
        return Result.ok(user);
    } 
    
}
