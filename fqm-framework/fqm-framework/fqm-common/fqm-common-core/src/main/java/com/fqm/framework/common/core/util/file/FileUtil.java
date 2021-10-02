/*
 * @(#)FileUtil.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-common-core
 * 创建日期 : 2021年4月16日
 * 修改历史 : 
 *     1. [2021年4月16日]创建文件 by 傅泉明
 */
package com.fqm.framework.common.core.util.file;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @version 
 * @author 傅泉明
 */
public class FileUtil {
    
    /**
     * 获取jar所在的路径
     * @return
     */
    public static String getJarPath() {
        String path = "";
        try {
            path = FileUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            path = URLDecoder.decode(path, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (path.startsWith("file:")) {
            path = path.substring(5, path.length());
        }
        
        if (System.getProperty("os.name").contains("dows")) {
            if (path.startsWith("/")) {
                path = path.substring(1, path.length());
            }
        }
        
        if (path.contains("jar")) {
            path = path.substring(0, path.lastIndexOf("."));
            return path.substring(0, path.lastIndexOf("/"));
        }
        return path.replace("target/classes/", "");
    }

    /**
     * 读取文件所有数据<br>
     * 文件的长度不能超过Integer.MAX_VALUE
     *
     * @param file 文件
     * @return 字节码
     */
    public static byte[] readFileToByteArray(final File file) throws IOException {
        return FileUtils.readFileToByteArray(file);
    }

    /**
     * 读取文件内容
     *
     * @param file    文件
     * @param charset 字符集
     * @return 内容
     */
    public static String readFileToString(final File file, final Charset charsetName) throws IOException {
        return FileUtils.readFileToString(file, charsetName);
    }

    /**
     * 读取文件内容
     *
     * @param file    文件
     * @param charset 字符集
     * @return 内容
     */
    public static String readFileToString(final File file, final String charsetName) throws IOException {
        return FileUtils.readFileToString(file, charsetName);
    }
    
    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param file       文件路径
     * @param charset    字符集
     * @return 文件中的每行内容的集合
     * @throws IOException 
     */
    public static List<String> readLines(File file, Charset charset) throws IOException {
        return FileUtils.readLines(file, charset);
    }
    
    /**
     * 从文件中读取每一行数据
     *
     * @param file       文件路径
     * @param charset    字符集
     * @return 文件中的每行内容的集合
     * @throws IOException 
     */
    public static List<String> readLines(File file, String charset) throws IOException {
        return FileUtils.readLines(file, charset);
    }
    
    /**
     * 写数据到文件中
     *
     * @param file 目标文件
     * @param data 数据
     * @throws IOException IO异常
     */
    public static void writeBytes(File file, byte[] data) throws IOException {
        writeBytes(file, data, 0, data.length, false);
    }

    /**
     * 写入数据到文件
     *
     * @param file     目标文件
     * @param data     数据
     * @param off      数据开始位置
     * @param len      数据长度
     * @param append   是否追加模式
     * @throws IOException IO异常
     */
    public static void writeBytes(File file, byte[] data, int off, int len, boolean append) throws IOException {
        FileUtils.writeByteArrayToFile(file, data, off, len, append);
    }
    
    /**
     * 将列表写入文件
     *
     * @param file     文件
     * @param lines     列表
     * @param charset  字符集
     * @throws IOException IO异常
     */
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines) throws IOException {
        FileUtils.writeLines(file, charsetName, lines, false);
    }
    
    /**
     * 将列表写入文件
     *
     * @param list     列表
     * @param file     文件
     * @param charset  字符集
     * @param append   是否追加
     * @throws IOException IO异常
     */
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines, final boolean append) throws IOException {
        FileUtils.writeLines(file, charsetName, lines, append);
    }

}
