package com.lyz.service;

import com.lyz.pojo.Flow;
import com.lyz.pojo.PageBean;

public interface FlowService {
    void add(Flow flow);

    PageBean<Flow> list(Integer pageNum, Integer pageSize, Integer categoryId, String state);
}
