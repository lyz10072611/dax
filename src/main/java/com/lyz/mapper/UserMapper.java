package com.lyz.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.lyz.pojo.User;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问层 - 适配水泥厂业务系统
 */
@Mapper
public interface UserMapper {
    
    // 根据用户名查询用户
    @Select("SELECT user_id as id, username, password_hash as password, email, " +
            "create_time as createTime, last_login_time as lastLoginTime, " +
            "status FROM users WHERE username = #{username}")
    User findByUserName(String username);
    
    // 根据邮箱查询用户
    @Select("SELECT user_id as id, username, password_hash as password, email, " +
            "create_time as createTime, last_login_time as lastLoginTime, " +
            "status FROM users WHERE email = #{email}")
    User findByEmail(String email);
    
    // 获取用户对应的roleCode - 通过user_roles表查询
    @Select("SELECT r.role_id FROM roles r JOIN user_roles ur ON r.role_id = ur.role_id " +
            "JOIN users u ON ur.user_id = u.user_id WHERE u.username = #{username}")
    Integer findRoleCodeByUsername(String username);

    // 根据用户ID查询其role_id - 通过user_roles表查询
    @Select("SELECT ur.role_id FROM user_roles ur WHERE ur.user_id = #{id}")
    Integer findRoleIdByUserId(Integer id);

    // 分页查询用户，可按用户名模糊查询
    @Select({
            "<script>",
            "SELECT user_id as id, username, email, create_time as createTime, last_login_time as lastLoginTime, status",
            "  FROM users",
            " <where>",
            "  <if test=\"username != null and username != ''\">",
            "    AND username LIKE CONCAT('%', #{username}, '%')",
            "  </if>",
            " </where>",
            " ORDER BY user_id DESC",
            "</script>"
    })
    java.util.List<User> listUsers(@Param("username") String username);

    // 管理员新增用户（需提前加密密码）
    @Insert("INSERT INTO users(username, password_hash, email, create_time, last_login_time, status) " +
            "VALUES(#{username}, #{password}, #{email}, NOW(), NULL, #{status})")
    void adminAdd(User user);

    // 管理员更新用户
    @Update("UPDATE users SET username = #{username}, email = #{email}, status = #{status} WHERE user_id = #{id}")
    void adminUpdate(User user);

    // 管理员删除用户
    @Delete("DELETE FROM users WHERE user_id = #{id}")
    void adminDelete(Integer id);
    
    // 用户注册
    @Insert("INSERT INTO users(username, password_hash, email, create_time, status) " +
            "VALUES (#{username}, #{password}, #{email}, NOW(), 1)")
    void add(String username, String password, String email);
    
    // 用户注册（带邮箱和电话）
    @Insert("INSERT INTO users(username, password_hash, email, create_time, status) " +
            "VALUES (#{username}, #{password}, #{email}, NOW(), 1)")
    void addWithEmailAndPhone(String username, String password, String email, String phone);

    // 更新用户基本信息
    @Update("UPDATE users SET email = #{email} WHERE user_id = #{id}")
    void update(User user);

    // 更新用户头像 - 由于数据库中没有user_pic字段，这个方法暂时不实现
    // @Update("UPDATE users SET user_pic = #{avatarUrl} WHERE user_id = #{id}")
    // void updateAvatar(String avatarUrl, Integer id);

    // 更新用户密码
    @Update("UPDATE users SET password_hash = #{md5String} WHERE user_id = #{id}")
    void updatePwd(String md5String, Integer id);

    // 增加用户下载次数 - 由于数据库中没有download_count字段，这个方法暂时不实现
    // @Update("UPDATE users SET download_count = download_count + #{delta} WHERE user_id = #{id}")
    // void incrSumDownloadBy(@Param("id") Integer id, @Param("delta") Integer delta);
    
    // 更新最后登录时间
    @Update("UPDATE users SET last_login_time = NOW() WHERE user_id = #{id}")
    void updateLastLoginTime(Integer id);
}
