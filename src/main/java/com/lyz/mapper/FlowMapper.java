package com.lyz.mapper;

import com.lyz.pojo.Flow;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FlowMapper {


    @Insert("insert into article(title,content,cover_img,state,category_id,create_user ," +
            "create_time,update_time)" +
            "values (#{title},#{content},#{coverImg},#{state},#{categoryId},#{createUser}," +
            "#{createTime},#{updateTime})")
    void add(Flow flow);


    List<Flow> list(Integer userId, Integer categoryId, String state);
}
