package com.system.powermanagement.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页结果
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {
    
    /**
     * 当前页码
     */
    private Long pageNum;
    
    /**
     * 每页记录数
     */
    private Long pageSize;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Long pages;
    
    /**
     * 数据列表
     */
    private List<T> list;
    
    /**
     * 构造分页结果
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @param total 总记录数
     * @param list 数据列表
     */
    public PageResult(Long pageNum, Long pageSize, Long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
        
        if (pageSize != null && pageSize > 0) {
            this.pages = (long) Math.ceil((double) total / pageSize);
        } else {
            this.pages = 0L; // 或者根据业务设定，如果pageSize无效，则页数为0或1
        }
    }
} 