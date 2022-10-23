package com.fqm.test.mq.model;

import java.io.Serializable;
import java.util.Date;

public class Dept implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
//    private Long id;
//    private String name;
    private Date date;
//    public Long getId() {
//        return id;
//    }
//    public void setId(Long id) {
//        this.id = id;
//    }
//    public String getName() {
//        return name;
//    }
//    public void setName(String name) {
//        this.name = name;
//    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    
}
