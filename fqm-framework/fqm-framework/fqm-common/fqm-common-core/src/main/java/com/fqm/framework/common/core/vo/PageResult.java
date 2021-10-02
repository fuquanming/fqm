package com.fqm.framework.common.core.vo;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * @version 
 * @author 傅泉明
 */
@ApiModel("分页结果")
public class PageResult<T> {

    @ApiModelProperty(value = "数据", required = true)
    private List<T> rows;

    @ApiModelProperty(value = "总量", required = true)
    private Long total;

    public List<T> getRows() {
        return rows;
    }

    public PageResult<T> setRows(List<T> rows) {
        this.rows = rows;
        return this;
    }

    public Long getTotal() {
        return total;
    }

    public PageResult<T> setTotal(Long total) {
        this.total = total;
        return this;
    }
    
}
