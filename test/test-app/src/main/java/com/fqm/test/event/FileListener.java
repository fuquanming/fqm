package com.fqm.test.event;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Db回滚（如：唯一索引），只会回调方法：AFTER_ROLLBACK
 * 业务异常（）回调：只会回调方法：AFTER_ROLLBACK
 * 
 * @version 
 * @author 傅泉明
 */
@Component
public class FileListener {

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT,// 同一个事务。异常会回滚当前事务
            classes = FileEvent.class)
    public void onSendEvent(FileEvent event) {
        if (event.getSource() instanceof List<?>) {
            List<String> list = (List<String>) event.getSource();
            System.out.println("--- event.size=" + list.size());
        }
        System.out.println("---BEFORE_COMMIT>" + event.getSource());
//        int i = 1/ 0;
        // 发送文件状态，请求过程异常则抛异常，回滚事务
    }
    
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT, // 如果方法执行db操作用新的事务PROPAGATION_REQUIRES_NEW，之前的事务已不能使用
            classes = FileEvent.class)
    public void onSendEvent2(FileEvent event) {
        System.out.println("---AFTER_COMMIT>" + event.getSource());
    }
    
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_ROLLBACK,
            classes = FileEvent.class)
    public void onRollbackEvent(FileEvent event) {
        System.out.println("---AFTER_ROLLBACK>" + event.getSource());
    }
    
    @EventListener(classes = FileEvent.class)
    public void onSendEventGeneral(FileEvent event) {
        System.out.println("---onSendEventGeneral>" + event.getSource());
    }
    
}
