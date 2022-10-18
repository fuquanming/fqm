package com.fqm.framework.common.core.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fqm.framework.common.core.exception.GlobalException;
import com.fqm.framework.common.core.exception.ServiceException;
import com.fqm.framework.common.core.lang.Filter;
/**
 * 
 * 
 * @version 
 * @author 傅泉明
 */
public class NetUtil {

    private NetUtil() {
    }
    /**
     * 获取所有满足过滤条件的本地IP地址对象
     *
     * @param addressFilter 过滤器，null表示不过滤，获取所有地址
     * @return 过滤后的地址对象列表
     * @since 4.5.17
     */
    public static Set<InetAddress> localAddressList(Filter<InetAddress> addressFilter) {
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new GlobalException(e);
        }

        if (networkInterfaces == null) {
            throw new ServiceException(null, "Get network interface error!");
        }

        final LinkedHashSet<InetAddress> ipSet = new LinkedHashSet<>();

        while (networkInterfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = networkInterfaces.nextElement();
            final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                final InetAddress inetAddress = inetAddresses.nextElement();
                if (null != inetAddress) {
                    if (null == addressFilter || addressFilter.accept(inetAddress)) {
                        ipSet.add(inetAddress);
                    }
                }
            }
        }

        return ipSet;
    }
    
    /**
     * 获得本机物理地址
     *
     * @return 本机物理地址
     * @since 5.7.3
     */
    public static byte[] getLocalHardwareAddress() {
        return getHardwareAddress(getLocalhost());
    }
    
    /**
     * 获得指定地址信息中的硬件地址
     *
     * @param inetAddress {@link InetAddress}
     * @return 硬件地址
     */
    public static byte[] getHardwareAddress(InetAddress inetAddress) {
        if (null == inetAddress) {
            return new byte[] {};
        }

        try {
            final NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
            if (null != networkInterface) {
                return networkInterface.getHardwareAddress();
            }
        } catch (SocketException e) {
            throw new GlobalException(e);
        }
        return new byte[] {};
    }

    
    /**
     * 获取本机网卡IP地址，规则如下：
     *
     * <pre>
     * 1. 查找所有网卡地址，必须非回路（loopback）地址、非局域网地址（siteLocal）、IPv4地址
     * 2. 如果无满足要求的地址，调用 {@link InetAddress#getLocalHost()} 获取地址
     * </pre>
     * <p>
     * 此方法不会抛出异常，获取失败将返回{@code null}<br>
     * <p>
     *
     * @return 本机网卡IP地址，获取失败返回{@code null}
     */
    public static InetAddress getLocalhost() {
        final LinkedHashSet<InetAddress> localAddressList = localAddressList(address -> {
            // 非loopback地址，指127.*.*.*的地址，需为IPV4地址
            return !address.isLoopbackAddress() && address instanceof Inet4Address;
        });

        if (CollectionUtil.isNotEmpty(localAddressList)) {
            InetAddress address2 = null;
            for (InetAddress inetAddress : localAddressList) {
                if (!inetAddress.isSiteLocalAddress()) {
                    // 非地区本地地址，指10.0.0.0 ~ 10.255.255.255、172.16.0.0 ~ 172.31.255.255、192.168.0.0 ~ 192.168.255.255
                    return inetAddress;
                } else if (null == address2) {
                    address2 = inetAddress;
                }
            }

            if (null != address2) {
                return address2;
            }
        }

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // ignore
        }

        return null;
    }
    
}
