package com.fqm.framework.common.lock.redis.listener.spring;
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

import com.fqm.framework.common.lock.constant.Constants;
import com.fqm.framework.common.lock.impl.RedisTemplateLock;
import com.fqm.framework.common.redis.listener.spring.RedisKeyDeleteEvent;
/**
 * 监听Redis 删除事件（过期的也会被监听到），发现删除锁时，唤醒一个被阻塞的线程
 * 
 * @version 
 * @author 傅泉明
 */
public class LockRedisKeyDeleteEventHandle {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @EventListener
    public void eventHandle(RedisKeyDeleteEvent event) {
        String deleteKey = new String(event.getSource());
        
        if (deleteKey.startsWith(Constants.PREFIX_KEY)) {
            logger.info("Delete lockKey=" + deleteKey);
            Set<Thread> blockThreads = RedisTemplateLock.BLOCK_THREADS.get(deleteKey);
            if (blockThreads == null) return;
            // 唤醒一个被阻塞的线程
            for (Iterator<Thread> iterator = blockThreads.iterator(); iterator.hasNext();) {
                Thread thread = iterator.next();
                logger.info("lockKey->{},unpark->{}", deleteKey, thread);
                LockSupport.unpark(thread);
                break;
            }
        }
    }
    
}
