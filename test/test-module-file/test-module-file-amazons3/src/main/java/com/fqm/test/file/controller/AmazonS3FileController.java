package com.fqm.test.file.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.fqm.framework.common.core.util.JsonUtil;
import com.fqm.framework.common.core.util.io.IoUtil;
import com.fqm.framework.common.core.vo.Result;
import com.fqm.framework.file.FileFactory;
import com.fqm.framework.file.FileMode;
import com.fqm.framework.file.model.FileObjectMetadata;
import com.fqm.framework.file.model.FileUploadRequest;
import com.fqm.framework.file.model.FileUploadResponse;
import com.fqm.framework.file.tag.FileTag;

import cn.hutool.core.date.DateUtil;

@CrossOrigin
@RestController
public class AmazonS3FileController {

    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Resource
    FileFactory fileFactory;

    @GetMapping("/file/s3/upload")
    @ResponseBody
    public Result<String> uploadFile() {
        File file = new File("C:\\Users\\fqm\\Pictures\\Saved Pictures\\1.png");
        file = new File("C:\\迅雷下载\\1850520260107153131A044.pdf");
        String fileId = fileFactory.getFileTemplate().uploadFile(file, "a/" + file.getName());
        return Result.ok(fileId);
    }
    
    @GetMapping("/file/s3/upload2")
    @ResponseBody
    public Result<String> uploadFile2() {
        File file = new File("C:\\Users\\fqm\\Pictures\\Saved Pictures\\1.png");
        file = new File("C:\\迅雷下载\\1850520260107153131A044.pdf");
        String fileId = "";
        try (FileInputStream fis = new FileInputStream(file);) {
//            FileUploadResponse fileUploadResponse = fileFactory.getFileTemplate()
//                    .uploadFile(null, fis, "a/" + file.getName(), 
//                    new FileObjectMetadata()
//                    .setContentType(Files.probeContentType(file.toPath()))
//                    .setCacheControl("setCacheControl")
//                    .setContentDisposition("setContentDisposition")
//                    .setContentEncoding("setContentEncoding")
//                            );
//            fileId = fileUploadResponse.getFileId();
            
            fileId = fileFactory.getFileTemplate()
                    .uploadFile(fis, "a/" + file.getName(), 
                    new FileObjectMetadata()
                    .setContentType(Files.probeContentType(file.toPath()))
                    .setCacheControl("setCacheControl")
                    .setContentDisposition("setContentDisposition")
                    .setContentEncoding("setContentEncoding")
                            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            
        }
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
    
    String bucketName = "config";
    String objectName = "a/" + "a.md";
    
    @PostMapping(value = "/file/s3/chunkUpload")
    public Result<FileChunksMergeDTO> uploadFile(
            FileUploadDTO fileUploadDTO,
            @RequestParam(value = "file", required = false) MultipartFile multipartFile
            ) throws Exception {
        log.info(JsonUtil.toJsonStr(fileUploadDTO));
        System.out.println(multipartFile.getName() + "\t" + multipartFile.getSize() + "\t" + multipartFile.getOriginalFilename()
         + "\t" + multipartFile.getContentType());

        String md5 = fileUploadDTO.getMd5();
        String fileMd5Key = "mufi_" + md5;
        String fileMd5LockKey = fileMd5Key + "_lock";
        String partResultKey = "partResult";
        String incrementKey = "increment";
        
        
        objectName = "a/" + multipartFile.getOriginalFilename();
        
        try {
            FileUploadRequest fileUploadRequest = new FileUploadRequest();
            if (fileUploadDTO.getChunk() != null) {
                fileUploadRequest.setChunk(fileUploadDTO.getChunk() + 1);
                fileUploadRequest.setChunks(fileUploadDTO.getChunks());
            }
            fileUploadRequest.setMd5(md5);
            fileUploadRequest.setSize(fileUploadDTO.getSize());
            
            FileObjectMetadata fileObjectMetadata = new FileObjectMetadata();
            if (fileUploadDTO.getChunk() != null) {
                // 分片
                fileObjectMetadata.setContentLength(fileUploadDTO.getSize())
                .setContentType(fileUploadDTO.getType());
            } else {
                // 非分片
                fileObjectMetadata.setContentLength(multipartFile.getSize())
                .setContentType(multipartFile.getContentType());
            }
            
            FileUploadResponse fileUploadResponse = fileFactory.getFileTemplate()
                    .uploadFile(fileUploadRequest, multipartFile.getInputStream(), objectName, 
                            fileObjectMetadata
                            );
            System.out.println(JsonUtil.toJsonStr(fileUploadResponse));
            
            FileChunksMergeDTO mergeDTO = new FileChunksMergeDTO();
            mergeDTO.setSubmittedFileName(multipartFile.getOriginalFilename());
            mergeDTO.setMd5(fileUploadDTO.getMd5());
            mergeDTO.setContextType(fileUploadDTO.getType());
            mergeDTO.setChunks(fileUploadDTO.getChunks());
            mergeDTO.setExt(fileUploadDTO.getExt());
            mergeDTO.setSize(fileUploadDTO.getSize());
            mergeDTO.setName(fileUploadDTO.getName());
            if (!fileUploadResponse.isChunkUploadStatus()) {
//                int i = 1 / 0;
                return Result.fail(mergeDTO);
            } else {
                return Result.ok(mergeDTO);
            }
        } finally {
            multipartFile.getInputStream().close();
        }
    }
    
    @GetMapping(value = "/file/s3/getTag")
    public Result<List<FileTag>> getTag(String fileId) throws Exception {
        return Result.ok(fileFactory.getFileTemplate(FileMode.AMAZONS3).getFileTag(fileId));
    }
    
    @GetMapping(value = "/file/s3/setTag")
    public Result<Boolean> setTag(String fileId) throws Exception {
        List<FileTag> fileTags = new ArrayList<>();
        fileTags.add(new FileTag("tag1", "tag1"));
        fileFactory.getFileTemplate(FileMode.AMAZONS3).setFileTag(fileId, fileTags);
        return Result.ok();
    }
    
    @GetMapping(value = "/file/s3/deleteTag")
    public Result<Boolean> deleteTag(String fileId) throws Exception {
        fileFactory.getFileTemplate(FileMode.AMAZONS3).deleteFileTag(fileId);
        return Result.ok();
    }
    
    @PostMapping(value = "/file/s3/chunkMerge")
    public Result<?> chunkMerge() throws Exception {
        return Result.ok();
    }
    
    @GetMapping(value = "/file/s3/chunk/{partNumber}")
    public Result<?> chunkPartNumber(@PathVariable("partNumber") Integer partNumber) throws Exception {
        Date currentDate = new Date();
        Long PRE_SIGN_URL_EXPIRE = 60 * 10 * 1000L;
        Date expireDate = DateUtil.offsetMillisecond(currentDate, PRE_SIGN_URL_EXPIRE.intValue());
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName)
                .withExpiration(expireDate).withMethod(HttpMethod.PUT);
        Map<String, String> params = new HashMap<>();
        params.put("partNumber", partNumber.toString());
        params.put("uploadId", "8ae8e07f-b0d8-4c3e-9657-eecdcccd8106");
        if (params != null) {
            params.forEach((key, val) -> request.addRequestParameter(key, val));
        }
//        URL preSignedUrl = client.generatePresignedUrl(request);
//        return Result.ok(preSignedUrl.toString());
        return Result.ok();
    }
}
