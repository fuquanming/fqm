package com.fqm.framework.locks.executor;
/**
 * 包装尝试获取锁，而被阻塞的线程
 * 
 * @version 
 * @author 傅泉明
 */
public class BlockLockThreadExecutor {
    /** 阻塞的线程 */
    private Thread blockThread;
    /** 是否删除通知 */
    private boolean deleteNotify;

    public Thread getBlockThread() {
        return blockThread;
    }

    public BlockLockThreadExecutor setBlockThread(Thread blockThread) {
        this.blockThread = blockThread;
        return this;
    }

    public boolean isDeleteNotify() {
        return deleteNotify;
    }

    public BlockLockThreadExecutor setDeleteNotify(boolean deleteNotify) {
        this.deleteNotify = deleteNotify;
        return this;
    }
    
    @Override
    public String toString() {
        return blockThread.toString() + "@deleteNotify=" + deleteNotify;
    }
    
}
