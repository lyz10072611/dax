package com.lyz.service;

import com.lyz.pojo.Category;
import com.lyz.pojo.Result;

import java.util.List;

public interface CategoryService {


    void add(Category category);
    List<Category> list();

    Category findById(Integer id);

    Result update(Category category);

    void delete(Integer id);
}
