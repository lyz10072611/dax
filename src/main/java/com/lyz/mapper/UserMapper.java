package com.lyz.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.lyz.pojo.User;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    //根据用户名查询用户
    @Select("select * from users where username=#{username}")
    User findByUserName(String username);
    //获取用户对应的roleCode
    @Select("select r.role_code from role r join users u on u.role_id=r.id where u.username=#{username}")
    Integer findRoleCodeByUsername(String username);

    // 根据用户ID查询其role_id
    @Select("select role_id from users where id=#{id}")
    Integer findRoleIdByUserId(Integer id);

    // 分页查询用户，可按用户名模糊
    @Select({
            "<script>",
            "select id,username,nickname,email,user_pic as userPic,role_id as roleId,",
            "       daily_download as dailyDownload,sum_download as sumDownload,",
            "       create_time as createTime,update_time as updateTime",
            "  from users",
            " <where>",
            "  <if test=\"username!=null and username!=''\">",
            "    and username like concat('%',#{username},'%')",
            "  </if>",
            " </where>",
            " order by id desc",
            "</script>"
    })
    java.util.List<User> listUsers(@Param("username") String username);

    // 管理员新增用户（需提前加密密码）
    @Insert("insert into users(username,password,nickname,email,user_pic,role_id,daily_download,sum_download,create_time,update_time) " +
            "values(#{username},#{password},#{nickname},#{email},#{userPic},#{roleId},#{dailyDownload},#{sumDownload},now(),now())")
    void adminAdd(User user);

    // 管理员更新用户（可更新 role_id）
    @Update("update users set username=#{username},nickname=#{nickname},email=#{email},user_pic=#{userPic},role_id=#{roleId},daily_download=#{dailyDownload},sum_download=#{sumDownload},update_time=now() where id=#{id}")
    void adminUpdate(User user);

    // 管理员删除用户
    @Delete("delete from users where id=#{id}")
    void adminDelete(Integer id);
    //添加
    @Insert("insert into users(username,password,role_id,create_time,update_time)"+
            " values (#{username},#{password},(select id from role where role_code=1),now(),now())"
    )
    void add(String username, String password);

    @Update("update users set nickname=#{nickname},email=#{email},update_time=#{updateTime} where id=#{id}")//这里的update_time是数据库中的；updateTime是实体类
    void update(User user);

    @Update("update users set user_pic=#{avatarUrl},update_time=now() where id=#{id}")
    void updateAvatar(String avatarUrl, Integer id);


    @Update("update users set password=#{md5String},update_time=now() where id=#{id}")
    void updatePwd(String md5String, Integer id);

    @Update("update users set sum_download = sum_download + #{delta} where id=#{id}")
    void incrSumDownloadBy(@Param("id") Integer id, @Param("delta") Integer delta);
}
