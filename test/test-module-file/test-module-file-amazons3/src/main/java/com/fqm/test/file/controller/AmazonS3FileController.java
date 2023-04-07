package com.fqm.test.file.controller;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.common.core.util.io.IoUtil;
import com.fqm.framework.common.core.vo.Result;
import com.fqm.framework.file.FileFactory;
import com.fqm.framework.file.FileMode;

@RestController
public class AmazonS3FileController {

    @Resource
    FileFactory fileFactory;

    @GetMapping("/file/s3/upload")
    @ResponseBody
    public Result<String> uploadFile() {
        File file = new File("D:\\Documents\\1.jpg");
        String fileId = fileFactory.getFileTemplate(FileMode.AMAZONS3).uploadFile(file, "2/my.jpg");
        return Result.ok(fileId);
    }

    @GetMapping("/file/s3/download")
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
        
        try (InputStream is = fileFactory.getFileTemplate(FileMode.AMAZONS3).downloadFile(fileId);) {
            int available = is.available();
            IoUtil.copyByNio(is, response.getOutputStream(), available);
            response.setContentLength(available);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.ok();
    }
    
    @GetMapping("/file/s3/delete")
    @ResponseBody
    public Result<Boolean> deleteFile(String fileId) {
        boolean flag = fileFactory.getFileTemplate(FileMode.AMAZONS3).deleteFile(fileId);
        return Result.ok(flag);
    }
    
    @GetMapping("/file/s3/deleteDir")
    @ResponseBody
    public Result<Boolean> deleteDir(String fileId) {
        boolean flag = fileFactory.getFileTemplate(FileMode.AMAZONS3).deleteDir(fileId);
        return Result.ok(flag);
    }

    @GetMapping("/file/s3/url")
    @ResponseBody
    public Result<String> getFileUrl(String fileId) {
        String fileUrl = fileFactory.getFileTemplate(FileMode.AMAZONS3).getFileUrl(fileId);
        return Result.ok(fileUrl);
    }
}
