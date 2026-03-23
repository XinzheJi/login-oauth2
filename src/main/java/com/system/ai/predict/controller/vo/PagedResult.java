package com.system.ai.predict.controller.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedResult<T> {
    private int page;
    private int size;
    private long total;
    private List<T> items;
}

