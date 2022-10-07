package com.fqm.framework.common.http.file;

import java.io.InputStream;

/**
 * 通过http发送文件
 * 
 * @version 
 * @author 傅泉明
 */
public class HttpInputStream {
    /** 上传的文件名 */
    private String fileName;
    /** 上传的流  */
    private InputStream is;
    
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public InputStream getIs() {
        return is;
    }
    public void setIs(InputStream is) {
        this.is = is;
    }
    
}
