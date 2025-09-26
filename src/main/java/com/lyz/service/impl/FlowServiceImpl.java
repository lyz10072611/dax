package com.lyz.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lyz.mapper.FlowMapper;
import com.lyz.pojo.Flow;
import com.lyz.pojo.PageBean;
import com.lyz.service.FlowService;
import com.lyz.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class FlowServiceImpl implements FlowService {
    @Autowired
    private FlowMapper flowMapper;


    @Override
    public void add(Flow flow) {
        Map map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        flow.setCreateUser(id);
        flow.setCreateTime(LocalDateTime.now());
        flow.setUpdateTime(LocalDateTime.now());
        flowMapper.add(flow);
    }

    @Override
    public PageBean<Flow> list(Integer pageNum, Integer pageSize, Integer categoryId, String state) {
        //1.创建PageBean对象
        PageBean<Flow> pb = new PageBean<>();

        //2.开启分页查询 PageHelper
        PageHelper.startPage(pageNum,pageSize);

        //3.调用mapper
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        List<Flow> as = flowMapper.list(userId,categoryId,state);
        //Page中提供了方法,可以获取PageHelper分页查询后 得到的总记录条数和当前页数据
        Page<Flow> p = (Page<Flow>) as;

        //把数据填充到PageBean对象中
        pb.setTotal(p.getTotal());
        pb.setItems(p.getResult());
        return pb;
    }
}
