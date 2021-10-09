package com.fqm.framework.mybatis.core.handler;

import java.util.Date;
import java.util.Objects;

import org.apache.ibatis.reflection.MetaObject;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.fqm.framework.common.core.util.IdUtil;

/**
 * 自动填充字段，createTime,updateTime,id
 * @version 
 * @author 傅泉明
 */
public class DefaultDBFieldHandler implements MetaObjectHandler {

    String createTime = "createTime";
    String updateTime = "updateTime";
    String id = "id";
    //mp执行添加操作，这个方法执行
    @Override
    public void insertFill(MetaObject metaObject) {
        if (Objects.nonNull(metaObject)) {
            Date date = new Date();
            
            if (metaObject.hasSetter(createTime) && Objects.isNull(getFieldValByName(createTime, metaObject))) {
                setFieldValByName(createTime, date, metaObject);
            }
            if (metaObject.hasSetter(updateTime) && Objects.isNull(getFieldValByName(updateTime, metaObject))) {
                setFieldValByName(updateTime, date, metaObject);
            }
            if (metaObject.hasSetter(id) && Objects.isNull(getFieldValByName(id, metaObject))) {
                setFieldValByName(id, IdUtil.getSnowflake().nextId(), metaObject);
                System.out.println(getFieldValByName(id, metaObject));
            }
//        this.setFieldValByName("version", 1, metaObject);
//        this.setFieldValByName("deleted", 0, metaObject);
        }
    }

    //mp执行修改操作，这个方法执行
    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasSetter(updateTime)) {
            setFieldValByName(updateTime, new Date(), metaObject);
        }
    }
}

