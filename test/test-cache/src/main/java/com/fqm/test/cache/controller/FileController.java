package com.fqm.test.cache.controller;

import java.io.File;

import javax.annotation.Resource;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.common.core.vo.R;
import com.fqm.framework.file.FileFactory;
import com.fqm.framework.file.FileMode;
import com.fqm.test.cache.config.FileConfig;

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
    public R<String> uploadFile() {
        File file = new File("D:\\Documents\\1.jpg");
        String fileId = fileFactory.getFileTemplate(FileMode.minio).uploadFile(file, "2/my.jpg");
        return R.ok(fileId);
    }

    @GetMapping("/file/download")
    @ResponseBody
    public R<Boolean> downloadFile(String fileId) {
        boolean flag = fileFactory.getFileTemplate(fileConfig.getA()).downloadFile(fileId, "D:\\Documents\\1-1.jpg");
        return R.ok(flag);
    }

    @GetMapping("/file/url")
    @ResponseBody
    public R<String> getFileUrl(String fileId) {
//        System.out.println(fileConfig.getA());
        String fileUrl = fileFactory.getFileTemplate(fileConfig.getA()).getFileUrl(fileId);
        return R.ok(fileUrl);
    }
}
