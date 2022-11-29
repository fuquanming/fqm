package com.fqm.test.file.controller;

import java.io.File;

import javax.annotation.Resource;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.common.core.vo.Result;
import com.fqm.framework.file.FileFactory;
import com.fqm.framework.file.FileMode;
import com.fqm.test.file.config.FileConfig;

@RestController
@EnableConfigurationProperties(FileConfig.class)
public class FileController {

    //    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    FileFactory fileFactory;

    @Resource
    FileConfig fileConfig;

    @GetMapping("/file/upload")
    @ResponseBody
    public Result<String> uploadFile() {
        File file = new File("D:\\Documents\\1.jpg");
        String fileId = fileFactory.getFileTemplate(FileMode.MINIO).uploadFile(file, "2/my.jpg");
        return Result.ok(fileId);
    }

    @GetMapping("/file/download")
    @ResponseBody
    public Result<Boolean> downloadFile(String fileId) {
        boolean flag = fileFactory.getFileTemplate(fileConfig.getStorage()).downloadFile(fileId, "D:\\Documents\\1-1.jpg");
        return Result.ok(flag);
    }
    
    @GetMapping("/file/delete")
    @ResponseBody
    public Result<Boolean> deleteFile(String fileId) {
        boolean flag = fileFactory.getFileTemplate(fileConfig.getStorage()).deleteFile(fileId);
        return Result.ok(flag);
    }

    @GetMapping("/file/url")
    @ResponseBody
    public Result<String> getFileUrl(String fileId) {
//        System.out.println(fileConfig.getA());
        String fileUrl = fileFactory.getFileTemplate(fileConfig.getStorage()).getFileUrl(fileId);
        return Result.ok(fileUrl);
    }
}
