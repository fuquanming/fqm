package com.fqm.test.file;

import com.fqm.framework.file.model.FileUploadRequest;

public class TestBoolean {

    public static boolean a(FileUploadRequest fileUploadRequest) {
        if (null == fileUploadRequest || null == fileUploadRequest.getChunk() || null == fileUploadRequest.getChunks()) {
            return false;
        }
        return true;
    }
    
    public static boolean b(FileUploadRequest fileUploadRequest) {
        return (null != fileUploadRequest && null != fileUploadRequest.getChunk() && null != fileUploadRequest.getChunks());
    }
    
    public static void main(String[] args) {
        FileUploadRequest fileUploadRequest = new FileUploadRequest();
//        fileUploadRequest.setChunk(1);
//        fileUploadRequest.setChunks(1);
        System.out.println(a(fileUploadRequest));
        System.out.println(b(fileUploadRequest));
    }

}
