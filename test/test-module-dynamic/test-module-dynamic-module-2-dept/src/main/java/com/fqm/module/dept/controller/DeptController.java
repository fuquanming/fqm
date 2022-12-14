package com.fqm.module.dept.controller;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.common.core.util.IdUtil;
import com.fqm.framework.common.core.vo.Result;
import com.fqm.module.dept.model.Dept;
import com.fqm.module.dept.service.DeptService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "部门管理")
@RestController
public class DeptController {

    @Resource
    DeptService deptService;
    
    @ApiOperation("新增用户")
    @GetMapping("/dept/insert")
    public Result<Dept> insertDept() {
        Dept data = new Dept();
        data.setId(IdUtil.getSnowflake().nextId());
        data.setCreateTime(new Date());
        deptService.insert(data);
        System.out.println("---insertDept222---");
        return Result.ok(data);
    }
    
    @ApiOperation("用户数")
    @GetMapping("/dept/count")
    public Result<Long> getDep() {
        long count = deptService.count();
        System.out.println("---getDept222---");
        return Result.ok(count);
    }
    
}
