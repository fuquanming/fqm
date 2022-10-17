/*
 * @(#)IpUtil.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-common-core
 * 创建日期 : 2021年3月19日
 * 修改历史 : 
 *     1. [2021年3月19日]创建文件 by 傅泉明
 */
package com.fqm.framework.common.core.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 
 * @version 
 * @author 傅泉明
 */
public class IpUtil {

    public static String LOCAL_127 = "127.0.0.1";
    /**
     * 根据网卡获得IP地址
     * @return
     * @throws SocketException
     */
    public static String getLocalIp() {
        String ip = null;
        try {
            ip = Inet4Address.getLocalHost().getHostAddress();
            if (ip != null && !"".equals(ip) && !LOCAL_127.equals(ip)) {
                return ip;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ip = null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                String name = intf.getName();
                if (!name.contains("docker") && !name.contains("lo")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        //获得IP
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            String ipaddress = inetAddress.getHostAddress().toString();
                            if (!ipaddress.contains("::") && !ipaddress.contains("0:0:") && !ipaddress.contains("fe80")) {
                                if (!LOCAL_127.equals(ip)) {
                                    ip = ipaddress;
                                }
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }
    
}
