/*
 * @(#)IOUtil.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-common-core
 * 创建日期 : 2021年3月18日
 * 修改历史 : 
 *     1. [2021年3月18日]创建文件 by 傅泉明
 */
package com.fqm.framework.common.core.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * 
 * @version 
 * @author 傅泉明
 */
public class IoUtil {
    
    /**
     * 默认缓存大小 8192,2 << 12
     */
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    
    /**
     * 数据流末尾
     */
    public static final int EOF = -1;
    
    /**
     * 拷贝流，不会关闭流
     *
     * @param in             输入流
     * @param out            输出流
     * @param bufferSize     缓存大小
     * @return 传输的byte数
     * @throws IOException IO异常
     */
    public static long copyByNio(InputStream in, OutputStream out, int bufferSize) throws IOException {
        return copy(Channels.newChannel(in), Channels.newChannel(out), bufferSize);
    }
    
    /**
     * 拷贝文件Channel，使用NIO，拷贝后不会关闭channel
     *
     * @param inChannel  {@link FileChannel}
     * @param outChannel {@link FileChannel}
     * @return 拷贝的字节数
     * @throws IOException IO异常
     * @since 5.5.3
     */
    public static long copy(FileChannel inChannel, FileChannel outChannel) throws IOException {
        try {
            return inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }
    
    /**
     * 拷贝流，使用NIO，不会关闭channel
     *
     * @param in  {@link ReadableByteChannel}
     * @param out {@link WritableByteChannel}
     * @return 拷贝的字节数
     * @throws IOException IO异常
     * @since 4.5.0
     */
    public static long copy(ReadableByteChannel in, WritableByteChannel out) throws IOException {
        return copy(in, out, DEFAULT_BUFFER_SIZE);
    }
    
    /**
     * 拷贝流，使用NIO，不会关闭channel
     *
     * @param in             {@link ReadableByteChannel}
     * @param out            {@link WritableByteChannel}
     * @param bufferSize     缓冲大小，如果小于等于0，使用默认
     * @return 拷贝的字节数
     * @throws IOException IO异常
     */
    public static long copy(ReadableByteChannel in, WritableByteChannel out, int bufferSize) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize <= 0 ? DEFAULT_BUFFER_SIZE : bufferSize);
        long size = 0;
        while (in.read(byteBuffer) != EOF) {
            byteBuffer.flip();// 写转读
            size += out.write(byteBuffer);
            byteBuffer.clear();
        }
        return size;
    }
    
}
