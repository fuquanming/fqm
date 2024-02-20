package com.fqm.test.event;

import org.springframework.context.ApplicationEvent;

public class FileEvent extends ApplicationEvent {

    private Object obj = null;
    
    public FileEvent(Object source) {
        super(source);
        this.obj = source;
    }
    
}
