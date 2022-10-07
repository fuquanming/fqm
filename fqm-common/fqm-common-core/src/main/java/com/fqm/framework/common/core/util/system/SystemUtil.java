package com.fqm.framework.common.core.util.system;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;

import com.fqm.framework.common.core.util.NetUtil;

/**
 * 获取系统属性
 * 
 * @version 
 * @author 傅泉明
 */
public class SystemUtil {

    /**
     * 取得Host的信息。
     *
     * @return {@link HostInfo}对象
     */
    public static String getHostAddress() {
        final InetAddress localhost = NetUtil.getLocalhost();
        if(null != localhost){
            return localhost.getHostAddress();
        }
        return null;
    }
    
    /**
     * 取得Host的信息。
     *
     * @return {@link HostInfo}对象
     */
    public static String getHostName() {
        final InetAddress localhost = NetUtil.getLocalhost();
        if(null != localhost){
            return localhost.getHostName();
        }
        return null;
    }
    
    /**
     * 获取当前进程 PID
     *
     * @return 当前进程 ID
     */
    public static long getCurrentPID() {
        return Long.parseLong(getRuntimeMXBean().getName().split("@")[0]);
    }
    
    /**
     * 返回Java虚拟机运行时系统相关属性
     *
     * @return {@link RuntimeMXBean}
     */
    public static RuntimeMXBean getRuntimeMXBean() {
        return ManagementFactory.getRuntimeMXBean();
    }
    
}
