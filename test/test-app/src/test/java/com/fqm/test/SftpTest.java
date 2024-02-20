package com.fqm.test;

import java.io.File;
import java.util.List;

import cn.hutool.extra.ssh.Sftp;

public class SftpTest {

    public static void main(String[] args) throws Exception {
        Sftp sftp = new Sftp("192.168.86.145", 22, "root", "aDRzNiWTy4O4ZEYr");
        List<String> ls = sftp.ls("/home/ftpuser");
        ls.forEach(str -> {
            System.out.println(str);
        });
        System.out.println(sftp.upload("/home/ftpuser/ftp/upload", new File("C:\\Users\\fqm\\Pictures\\3.png")));
        System.out.println(sftp.delFile("/home/ftpuser/ftp/upload/3.png"));
        
        sftp.download("/home/ftpuser/ftp/upload/2.png", new File("C:\\Users\\fqm\\Pictures\\2-2.png"));
        System.out.println(sftp.delDir("/home/ftpuser/ftp/upload/file"));
        sftp.close();
    }

}
