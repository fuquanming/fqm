package com.fqm.framework.locks.redis.listener.spring;
/**
 * 监听Redis 删除key事件，唤醒一个被阻塞的线程
 * 
 * @version 
 * @author 傅泉明
 */

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import com.fqm.framework.common.redis.listener.spring.RedisKeyDeleteEvent;
import com.fqm.framework.locks.constant.Constants;
import com.fqm.framework.locks.executor.BlockLockThreadExecutor;
import com.fqm.framework.locks.impl.RedisLock;
/**
 * 监听Redis 删除事件（过期的也会被监听到），发现删除锁时，唤醒一个被阻塞的线程
 * 
 * @version 
 * @author 傅泉明
 */
public class LockRedisKeyDeleteEventHandle {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @EventListener
    public void deleteEventHandle(RedisKeyDeleteEvent event) {
        String deleteKey = new String(event.getSource());
        
        if (deleteKey.startsWith(Constants.PREFIX_KEY)) {
            logger.info("Delete lockKey=" + deleteKey);
            Set<BlockLockThreadExecutor> blockThreads = RedisLock.BLOCK_THREADS.get(deleteKey);
            if (blockThreads == null) return;
            // 唤醒一个被阻塞的线程，标识删除通知唤醒
            for (Iterator<BlockLockThreadExecutor> iterator = blockThreads.iterator(); iterator.hasNext();) {
                BlockLockThreadExecutor thread = iterator.next();
                logger.debug("lockKey->{},unpark->{}", deleteKey, thread);
                thread.setDeleteNotify(true);
                LockSupport.unpark(thread.getBlockThread());
                break;
            }
        }
    }
    
}
