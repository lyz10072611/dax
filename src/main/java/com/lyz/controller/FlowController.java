package com.lyz.controller;

import com.lyz.pojo.Flow;
import com.lyz.pojo.PageBean;
import com.lyz.pojo.Result;
import com.lyz.service.FlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flow")
public class FlowController {
    @Autowired
    private FlowService flowService;
    @PostMapping
    public Result add(@RequestBody @Validated Flow flow){
        flowService.add(flow);
        return Result.success();
    }
    @GetMapping
    public Result<PageBean<Flow>> list(
            Integer pageNum,
            Integer pageSize,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String state
    ) {
        PageBean<Flow> pb =  flowService.list(pageNum,pageSize,categoryId,state);
        return Result.success(pb);
    }
}

