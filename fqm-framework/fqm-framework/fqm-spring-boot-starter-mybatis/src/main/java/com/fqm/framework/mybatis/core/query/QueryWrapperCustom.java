package com.fqm.framework.mybatis.core.query;

import java.util.Collection;

import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

/**
 * 拓展 MyBatis Plus QueryWrapper 类，主要增加如下功能：
 *
 * 1. 拼接条件的方法，增加 xxxIfPresent 方法，用于判断值不存在的时候，不要拼接到条件中。
 * @version 
 * @author 傅泉明
 */
public class QueryWrapperCustom <T> extends QueryWrapper<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public QueryWrapperCustom<T> likeIfPresent(String column, String val) {
        if (StringUtils.hasText(val)) {
            return (QueryWrapperCustom<T>) super.like(column, val);
        }
        return this;
    }

    public QueryWrapperCustom<T> inIfPresent(String column, Collection<?> values) {
        if (!CollectionUtils.isEmpty(values)) {
            return (QueryWrapperCustom<T>) super.in(column, values);
        }
        return this;
    }

    public QueryWrapperCustom<T> inIfPresent(String column, Object... values) {
        if (!ArrayUtils.isEmpty(values)) {
            return (QueryWrapperCustom<T>) super.in(column, values);
        }
        return this;
    }

    public QueryWrapperCustom<T> eqIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperCustom<T>) super.eq(column, val);
        }
        return this;
    }

    public QueryWrapperCustom<T> gtIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperCustom<T>) super.gt(column, val);
        }
        return this;
    }

    public QueryWrapperCustom<T> betweenIfPresent(String column, Object val1, Object val2) {
        if (val1 != null && val2 != null) {
            return (QueryWrapperCustom<T>) super.between(column, val1, val2);
        }
        if (val1 != null) {
            return (QueryWrapperCustom<T>) ge(column, val1);
        }
        if (val2 != null) {
            return (QueryWrapperCustom<T>) le(column, val2);
        }
        return this;
    }

    // ========== 重写父类方法，方便链式调用 ==========

    @Override
    public QueryWrapperCustom<T> eq(boolean condition, String column, Object val) {
        super.eq(condition, column, val);
        return this;
    }

    @Override
    public QueryWrapperCustom<T> eq(String column, Object val) {
        super.eq(column, val);
        return this;
    }

    @Override
    public QueryWrapperCustom<T> last(String lastSql) {
        super.last(lastSql);
        return this;
    }

}
