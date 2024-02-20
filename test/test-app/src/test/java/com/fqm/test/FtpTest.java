package com.fqm.test;

import java.io.File;
import java.util.List;

import cn.hutool.extra.ftp.Ftp;

public class FtpTest {

    public static void main(String[] args) throws Exception {
        Ftp ftp = new Ftp("192.168.86.145", 21, "ftpuser", "123456");
//        List<String> ls = ftp.ls("/");
//        ls.forEach(str -> {
//            System.out.println(str);
//        });
        boolean flag = ftp.upload("/upload/file/a", new File("C:\\Users\\fqm\\Pictures\\1.png"));
        System.out.println(flag);
//        System.out.println(ftp.delFile("/upload/1.png"));
//        ftp.download("/upload/2.png", new File("C:\\Users\\fqm\\Pictures\\2-1.png"));
//        ftp.mkDirs("/upload/file/a");
//        ftp.mkDirs("/upload/file/b");
        
//        System.out.println(ftp.delDir("/upload/file/b"));
        ftp.close();
    }

}
