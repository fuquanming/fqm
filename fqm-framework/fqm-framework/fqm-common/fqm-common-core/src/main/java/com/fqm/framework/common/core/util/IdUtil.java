package com.fqm.framework.common.core.util;

import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.framework.common.core.incrementer.Snowflake;

/**
 * ID生成器工具类
 * 
 * @version 
 * @author 傅泉明
 */
public class IdUtil {

    /**
     * 获取数据中心ID<br>
     * 数据中心ID依赖于本地网卡MAC地址。
     * <p>
     * 此算法来自于mybatis-plus#Sequence
     * </p>
     *
     * @param maxDatacenterId 最大的中心ID
     * @return 数据中心ID
     */
    public static long getDataCenterId(long maxDatacenterId) {
        long id = 1L;
        final byte[] mac = NetUtil.getLocalHardwareAddress();
        if (null != mac) {
            id = ((0x000000FF & (long) mac[mac.length - 2])
                    | (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
            id = id % (maxDatacenterId + 1);
        }

        return id;
    }

    /**
     * 获取机器ID，使用进程ID配合数据中心ID生成<br>
     * 机器依赖于本进程ID或进程名的Hash值。
     *
     * <p>
     * 此算法来自于mybatis-plus#Sequence
     * </p>
     *
     * @param datacenterId 数据中心ID
     * @param maxWorkerId  最大的机器节点ID
     * @return ID
     */
    public static long getWorkerId(long datacenterId, long maxWorkerId) {
        final StringBuilder mpid = new StringBuilder();
        mpid.append(datacenterId);
        try {
            // 获取进程ID
            int pid = 0;
            String processName = ManagementFactory.getRuntimeMXBean().getName();
            if (StringUtil.isBlank(processName)) {
                throw new RuntimeException("Process name is blank!");
            }
            int atIndex = processName.indexOf('@');
            if (atIndex > 0) {
                pid = Integer.parseInt(processName.substring(0, atIndex));
            } else {
                pid = processName.hashCode();
            }
            
            mpid.append(pid);
        } catch (Exception igonre) {
            //ignore
        }
        /*
         * MAC + PID 的 hashcode 获取16个低位
         */
        return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }
    
    /**
     * 获取单例的Twitter的Snowflake 算法生成器对象<br>
     * 分布式系统中，有一些需要使用全局唯一ID的场景，有些时候我们希望能使用一种简单一些的ID，并且希望ID能够按照时间有序生成。
     *
     * <p>
     * snowflake的结构如下(每部分用-分开):<br>
     *
     * <pre>
     * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
     * </pre>
     * <p>
     * 第一位为未使用，接下来的41位为毫秒级时间(41位的长度可以使用69年)<br>
     * 然后是5位datacenterId和5位workerId(10位的长度最多支持部署1024个节点）<br>
     * 最后12位是毫秒内的计数（12位的计数顺序号支持每个节点每毫秒产生4096个ID序号）
     *
     * <p>
     * 参考：http://www.cnblogs.com/relucent/p/4955340.html
     *
     */
    public static Snowflake getSnowflake() {
        return SnowflakeInstance.instance;
    }
    
    public static class SnowflakeInstance {
        
        private static Logger logger = LoggerFactory.getLogger(SnowflakeInstance.class);
        
        public static Snowflake instance = null;
        
        static {
            instance = new Snowflake();
            logger.info("Snowflake dataCenterId={},workerId={},beginTime={}", instance.getDataCenterId(), 
                    instance.getWorkerId(), instance.getTwepoch());
        }
        
    }
    
}
