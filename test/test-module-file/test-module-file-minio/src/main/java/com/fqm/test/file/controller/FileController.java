package com.fqm.test.file.controller;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fqm.framework.common.core.util.io.IoUtil;
import com.fqm.framework.common.core.vo.Result;
import com.fqm.framework.file.FileFactory;
import com.fqm.framework.file.FileMode;
import com.fqm.test.file.config.FileConfig;

@CrossOrigin
@RestController
@EnableConfigurationProperties(FileConfig.class)
public class FileController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    FileFactory fileFactory;

    @Resource
    FileConfig fileConfig;

    @RequestMapping(value = "/file/upload")
    public Result<String> uploadFile(@RequestParam(name = "source", defaultValue = "inner") String source,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(value = "folderId", required = false) Long folderId,
            @RequestParam(value = "file", required = false) MultipartFile simpleFile) {
        //1，先将文件存在本地,并且生成文件名
        if (simpleFile != null) {
            logger.info("contentType={}, name={} , sfname={} , size={}", 
                    simpleFile.getContentType(), 
                    simpleFile.getName(), 
                    simpleFile.getOriginalFilename(),
                    simpleFile.getSize());
        }
        File file = new File("D:\\Documents\\1.jpg");
        System.out.println("uploadFile......");
//        String fileId = fileFactory.getFileTemplate(FileMode.MINIO).uploadFile(file, "2/my.jpg");
//        return Result.ok(fileId);
        return Result.ok();
    }

    @GetMapping("/file/download")
    @ResponseBody
    public Result<Boolean> downloadFile(String fileId, HttpServletResponse response) {
//        boolean flag = fileFactory.getFileTemplate(fileConfig.getStorage()).downloadFile(fileId, "D:\\Documents\\1-1.jpg");
//        return Result.ok(flag);
        
        String fileName = fileId.substring(fileId.lastIndexOf("/") + 1);
        String encode = null;
        try {
            encode = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        encode = encode.replaceAll("\\+", "%20");
        
        String percentEncodedFileName = encode;
        
        System.out.println("percentEncodedFileName=" + percentEncodedFileName);

        StringBuilder contentDispositionValue = new StringBuilder();
        contentDispositionValue.append("attachment; filename=")
            .append(percentEncodedFileName)
            .append(";")
            .append("filename*=")
            .append("utf-8''")
            .append(percentEncodedFileName);

        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
        response.setHeader("Content-disposition", contentDispositionValue.toString());
        response.setHeader("download-filename", percentEncodedFileName);
        
        try (InputStream is = fileFactory.getFileTemplate(FileMode.MINIO).downloadFile(fileId);) {
            int available = is.available();
            IoUtil.copyByNio(is, response.getOutputStream(), available);
            response.setContentLength(available);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.ok();
    }
    
    @GetMapping("/file/delete")
    @ResponseBody
    public Result<Boolean> deleteFile(String fileId) {
        boolean flag = fileFactory.getFileTemplate(FileMode.MINIO).deleteFile(fileId);
        return Result.ok(flag);
    }
    
    @GetMapping("/file/deleteDir")
    @ResponseBody
    public Result<Boolean> deleteDir(String fileId) {
        boolean flag = fileFactory.getFileTemplate(FileMode.MINIO).deleteDir(fileId);
        return Result.ok(flag);
    }

    @GetMapping("/file/url")
    @ResponseBody
    public Result<String> getFileUrl(String fileId) {
//        System.out.println(fileConfig.getA());
        String fileUrl = fileFactory.getFileTemplate(FileMode.MINIO).getFileUrl(fileId);
        return Result.ok(fileUrl);
    }
}
