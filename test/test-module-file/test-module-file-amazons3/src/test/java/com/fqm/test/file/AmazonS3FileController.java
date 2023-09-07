//package com.fqm.test.file;
//
//import java.io.File;
//import java.io.InputStream;
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.commons.lang3.reflect.FieldUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.data.redis.core.BoundHashOperations;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
//import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
//import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
//import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
//import com.amazonaws.services.s3.model.PartETag;
//import com.amazonaws.services.s3.model.UploadPartRequest;
//import com.amazonaws.services.s3.model.UploadPartResult;
//import com.fqm.framework.common.core.util.JsonUtil;
//import com.fqm.framework.common.core.util.io.IoUtil;
//import com.fqm.framework.common.core.vo.Result;
//import com.fqm.framework.file.FileFactory;
//import com.fqm.framework.file.FileMode;
//import com.fqm.framework.file.amazons3.AmazonS3Service;
//import com.fqm.framework.file.template.AmazonS3FileTemplate;
//
//@CrossOrigin
//@RestController
//public class AmazonS3FileController {
//
//    private Logger log = LoggerFactory.getLogger(getClass());
//    
//    @Resource
//    FileFactory fileFactory;
//
//    @GetMapping("/file/s3/upload")
//    @ResponseBody
//    public Result<String> uploadFile() {
//        File file = new File("D:\\Documents\\1.jpg");
//        String fileId = fileFactory.getFileTemplate(FileMode.AMAZONS3).uploadFile(file, "2/my.jpg");
//        return Result.ok(fileId);
//    }
//
//    @GetMapping("/file/s3/download")
//    @ResponseBody
//    public Result<Boolean> downloadFile(String fileId, HttpServletResponse response) {
////        boolean flag = fileFactory.getFileTemplate(fileConfig.getStorage()).downloadFile(fileId, "D:\\Documents\\1-1.jpg");
////        return Result.ok(flag);
//        String fileName = fileId.substring(fileId.lastIndexOf("/") + 1);
//        String encode = null;
//        try {
//            encode = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
//        } catch (UnsupportedEncodingException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
//        encode = encode.replaceAll("\\+", "%20");
//        
//        String percentEncodedFileName = encode;
//        
//        System.out.println("percentEncodedFileName=" + percentEncodedFileName);
//
//        StringBuilder contentDispositionValue = new StringBuilder();
//        contentDispositionValue.append("attachment; filename=")
//            .append(percentEncodedFileName)
//            .append(";")
//            .append("filename*=")
//            .append("utf-8''")
//            .append(percentEncodedFileName);
//
//        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
//        response.setHeader("Content-disposition", contentDispositionValue.toString());
//        response.setHeader("download-filename", percentEncodedFileName);
//        
//        try (InputStream is = fileFactory.getFileTemplate(FileMode.AMAZONS3).downloadFile(fileId);) {
//            int available = is.available();
//            IoUtil.copyByNio(is, response.getOutputStream(), available);
//            response.setContentLength(available);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return Result.ok();
//    }
//    
//    @GetMapping("/file/s3/delete")
//    @ResponseBody
//    public Result<Boolean> deleteFile(String fileId) {
//        boolean flag = fileFactory.getFileTemplate(FileMode.AMAZONS3).deleteFile(fileId);
//        return Result.ok(flag);
//    }
//    
//    @GetMapping("/file/s3/deleteDir")
//    @ResponseBody
//    public Result<Boolean> deleteDir(String fileId) {
//        boolean flag = fileFactory.getFileTemplate(FileMode.AMAZONS3).deleteDir(fileId);
//        return Result.ok(flag);
//    }
//
//    @GetMapping("/file/s3/url")
//    @ResponseBody
//    public Result<String> getFileUrl(String fileId) {
//        String fileUrl = fileFactory.getFileTemplate(FileMode.AMAZONS3).getFileUrl(fileId);
//        return Result.ok(fileUrl);
//    }
//    
//    
//    Map<String, List<PartETag>> partTagMap = new HashMap<>();
//    
//    String bucketName = "workflow";
//    String objectName = "a/" + "a.md";
//    
//    
//    AmazonS3Service amazonS3Service;
//    AmazonS3 client;
//    
//    // 创建InitiateMultipartUploadRequest对象。
//    InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);
//    // 初始化分片。
//    InitiateMultipartUploadResult upresult = null;
//    
////    @PostMapping
////    public void init() throws Exception {
////        AmazonS3FileTemplate fileTemplate = (AmazonS3FileTemplate) fileFactory.getFileTemplate(FileMode.AMAZONS3);
////        amazonS3Service = (AmazonS3Service) FieldUtils.readField(fileTemplate, "service", true);
////        client = (AmazonS3) FieldUtils.readField(amazonS3Service, "client", true);
////        upresult = client.initiateMultipartUpload(request);
////    }
//    
//    @Resource
//    StringRedisTemplate stringRedisTemplate;
//    
//    @PostMapping(value = "/file/s3/chunkUpload")
//    public Result<FileChunksMergeDTO> uploadFile(
//            FileUploadDTO fileUploadDTO,
//            @RequestParam(value = "file", required = false) MultipartFile multipartFile
//            ) throws Exception {
//
////        if (multipartFile == null || multipartFile.isEmpty()) {
////            log.error("分片上传分片为空");
////            return Result.fail(null);
////        }
//        System.out.println(JsonUtil.toJsonStr(fileUploadDTO));
//        System.out.println(multipartFile.getName() + "\t" + multipartFile.getSize() + "\t" + multipartFile.getOriginalFilename());
//
////        AmazonS3FileTemplate fileTemplate = (AmazonS3FileTemplate) fileFactory.getFileTemplate(FileMode.AMAZONS3);
////        amazonS3Service = (AmazonS3Service) FieldUtils.readField(fileTemplate, "service", true);
////        AmazonS3 client = (AmazonS3) FieldUtils.readField(amazonS3Service, "client", true);
//        
//        String md5 = fileUploadDTO.getMd5();
//        
//        AmazonS3FileTemplate fileTemplate = (AmazonS3FileTemplate) fileFactory.getFileTemplate(FileMode.AMAZONS3);
//        amazonS3Service = (AmazonS3Service) FieldUtils.readField(fileTemplate, "service", true);
//        client = (AmazonS3) FieldUtils.readField(amazonS3Service, "client", true);
//        
//        BoundHashOperations boundHashOps = stringRedisTemplate.boundHashOps("multipartUploadFileInfo");
//        Object initiateMultipartUploadResultObj = boundHashOps.get(md5);
//        if (initiateMultipartUploadResultObj == null) {
//            // lock
//            upresult = client.initiateMultipartUpload(request);
//            initiateMultipartUploadResultObj = JsonUtil.toJsonStr(upresult);
//            boundHashOps.put(md5, initiateMultipartUploadResultObj);
//            // unlock
//        } else {
//            upresult = JsonUtil.toBean(initiateMultipartUploadResultObj.toString(), InitiateMultipartUploadResult.class);
//        }
//        
////        if (upresult == null) {
////            upresult = client.initiateMultipartUpload(request);
////        }
//        
//        // 返回uploadId，它是分片上传事件的唯一标识。您可以根据该uploadId发起相关的操作，例如取消分片上传、查询分片上传等。
//        String uploadId = upresult.getUploadId();
//        // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
//        List<PartETag> partETags = partTagMap.get(fileUploadDTO.getMd5());
//        if (partETags == null) {
//            partETags = new ArrayList<>();
//            partTagMap.put(fileUploadDTO.getMd5(), partETags);
//        }
//        
//        // 每个分片的大小，用于计算文件有多少个分片。单位为字节。
//        final long partSize = 10 * 1024L;   //1 KB。
//        
//        UploadPartRequest uploadPartRequest = new UploadPartRequest();
//        uploadPartRequest.setBucketName(bucketName);
//        uploadPartRequest.setKey(objectName);
//        uploadPartRequest.setUploadId(uploadId);
//        
////        File file = new File("d://test//" + fileUploadDTO.getChunk() + "." + fileUploadDTO.getExt());
////        multipartFile.transferTo(file);
////        uploadPartRequest.setFile(file);
//        
//        uploadPartRequest.setInputStream(multipartFile.getInputStream());
//        // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为10 KB。
//        uploadPartRequest.setPartSize(multipartFile.getSize());
//        // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出此范围，OSS将返回InvalidArgument错误码。
//        uploadPartRequest.setPartNumber(fileUploadDTO.getChunk() + 1);
//        // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
//        long partTime = System.currentTimeMillis();
//        UploadPartResult uploadPartResult = client.uploadPart(uploadPartRequest);
//        // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
//        partETags.add(uploadPartResult.getPartETag());
//        System.out.println("part=" + (System.currentTimeMillis() - partTime) + "\t" + JsonUtil.toJsonStr(partETags));
//        System.out.println(fileUploadDTO.getChunks().intValue() == (fileUploadDTO.getChunk() + 1));
//        
//        
//        if (fileUploadDTO.getChunks().intValue() == fileUploadDTO.getChunk() + 1) {
//            // 创建CompleteMultipartUploadRequest对象。
//            // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
//            CompleteMultipartUploadRequest completeMultipartUploadRequest =
//                    new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);
//            partTagMap.remove(fileUploadDTO.getMd5());
//            
////            upresult = client.initiateMultipartUpload(request);
//            // 取消分片上传
////            client.abortMultipartUpload(null);
//            
//            boundHashOps.delete(md5);
//            long beginTime = System.currentTimeMillis();
//            // 完成分片上传。
//            CompleteMultipartUploadResult completeMultipartUploadResult = client.completeMultipartUpload(completeMultipartUploadRequest);
//            System.out.println("comlete=" + (System.currentTimeMillis() - beginTime) + "\t" + completeMultipartUploadResult);
//        }
//        
//        FileChunksMergeDTO mergeDTO = new FileChunksMergeDTO();
//        mergeDTO.setSubmittedFileName(multipartFile.getOriginalFilename());
//        mergeDTO.setMd5(fileUploadDTO.getMd5());
//        mergeDTO.setContextType(fileUploadDTO.getType());
//        mergeDTO.setChunks(fileUploadDTO.getChunks());
//        mergeDTO.setExt(fileUploadDTO.getExt());
//        mergeDTO.setSize(fileUploadDTO.getSize());
//        mergeDTO.setName(fileUploadDTO.getName());
//        return Result.ok(mergeDTO);
//        
//        
////        //  存放分片文件的服务器绝对路径 ，例如 D:\\uploadfiles\\2020\\04
////        String uploadFolder = FileDataTypeUtil.getUploadPathPrefix(fileProperties.getStoragePath());
////
////        if (fileUploadDTO.getChunks() == null || fileUploadDTO.getChunks() <= 0) {
////            //没有分片，按照普通文件上传处理
////            File file = fileStrategy.upload(multipartFile);
////            file.setFileMd5(fileUploadDTO.getMd5());
////            
////            fileService.save(file);
////
////            return success(null);
////        } else {
////            //为上传的文件准备好对应的位置
////            java.io.File targetFile = webUploader.getReadySpace(fileUploadDTO, uploadFolder);
////
////            if (targetFile == null) {
////                return fail("分片上传失败");
////            }
////            //保存上传文件
////            multipartFile.transferTo(targetFile);
////
////            //封装信息给前端，用于分片合并
////            FileChunksMergeDTO mergeDTO = new FileChunksMergeDTO();
////            mergeDTO.setSubmittedFileName(multipartFile.getOriginalFilename());
////            dozerUtils.map(fileUploadDTO,mergeDTO);
////
////            return success(mergeDTO);
////        }
//    }
//    
//    @PostMapping(value = "/file/s3/chunkMerge")
//    public Result<?> chunkMerge(
//            FileChunksMergeDTO fileChunksMergeDTO) throws Exception {
//        System.err.println(JsonUtil.toJsonStr(fileChunksMergeDTO));
//        Thread.sleep(3000);
//        return Result.ok(new com.fqm.test.file.controller.File());
//    }
//}
