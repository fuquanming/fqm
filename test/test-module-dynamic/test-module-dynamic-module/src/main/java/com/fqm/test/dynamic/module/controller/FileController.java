package com.fqm.test.dynamic.module.controller;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fqm.framework.common.core.vo.Result;
import com.fqm.test.dynamic.module.service.UserService;

@RestController
public class FileController {

    @Resource
    UserService userService;
    
    @PostConstruct
    public void init() {
        System.out.println("---FileController---");
    }
    
    @PostMapping("/test/upload")
    @ResponseBody
    public Result<String> uploadFile(@RequestParam("1.jpg") MultipartFile file) {
        System.out.println("file:" + file.getSize());
        return Result.ok(file.getOriginalFilename());
    }
    
    @PostMapping("/test/upload2")
    @ResponseBody
    public Result<String> uploadFile2(@RequestParam("1") MultipartFile file) {
        System.out.println("file2:" + file.getSize());
        return Result.ok(file.getOriginalFilename());
    }
    
    @PostMapping("/test/upload3")
    @ResponseBody
    public Result<String> uploadFile3(MultipartFile file) {
        System.out.println("file3:" + file.getSize());
        return Result.ok(file.getOriginalFilename());
    }
    
}
