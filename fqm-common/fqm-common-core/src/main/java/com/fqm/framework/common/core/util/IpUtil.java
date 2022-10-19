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

    private static final String LOCAL_127 = "127.0.0.1";
    
    private static String ipColon = "::";
    private static String ipColonZero = "0:0:";
    private static String ipFe = "fe80";
    
    private IpUtil() {
    }
    
    private static String getHostAddressIp() {
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            if (ip != null && !"".equals(ip) && !LOCAL_127.equals(ip)) {
                return ip;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }
    
    private static String getInetAddressIp(InetAddress inetAddress) {
        String ip = null;
        if (!inetAddress.isLoopbackAddress()) {
            String ipaddress = inetAddress.getHostAddress();
            if (!ipaddress.contains(ipColon) && !ipaddress.contains(ipColonZero) && !ipaddress.contains(ipFe)) {
                ip = ipaddress;
            }
        }
        return ip;
    }
    
    /**
     * 根据网卡获得IP地址
     * @return
     * @throws SocketException
     */
    public static String getLocalIp() {
        String ip = getHostAddressIp();
        if (null != ip) {
            return ip;
        }
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                String name = intf.getName();
                if (!name.contains("docker") && !name.contains("lo")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        //获得IP
                        ip = getInetAddressIp(enumIpAddr.nextElement());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }
    
}
