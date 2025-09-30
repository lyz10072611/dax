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
    @Select("SELECT user_id as id, username, password_hash as password, email, nickname, " +
            "user_pic as userPic, role_id as roleId, " +
            "create_time as createTime, last_login_time as lastLoginTime, " +
            "status FROM users WHERE username = #{username}")
    User findByUserName(String username);
    
    // 获取用户对应的roleCode
    @Select("SELECT r.role_code FROM roles r JOIN users u ON u.role_id = r.role_id WHERE u.username = #{username}")
    Integer findRoleCodeByUsername(String username);

    // 根据用户ID查询其role_id
    @Select("SELECT role_id FROM users WHERE user_id = #{id}")
    Integer findRoleIdByUserId(Integer id);

    // 分页查询用户，可按用户名模糊查询
    @Select({
            "<script>",
            "SELECT user_id as id, username, nickname, email, user_pic as userPic, role_id as roleId,",
            "       create_time as createTime, last_login_time as lastLoginTime, status",
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
    @Insert("INSERT INTO users(username, password_hash, nickname, email, user_pic, role_id, " +
            "create_time, last_login_time, status) " +
            "VALUES(#{username}, #{password}, #{nickname}, #{email}, #{userPic}, #{roleId}, " +
            "NOW(), NULL, #{status})")
    void adminAdd(User user);

    // 管理员更新用户（可更新 role_id）
    @Update("UPDATE users SET username = #{username}, nickname = #{nickname}, email = #{email}, " +
            "user_pic = #{userPic}, role_id = #{roleId}, status = #{status} WHERE user_id = #{id}")
    void adminUpdate(User user);

    // 管理员删除用户
    @Delete("DELETE FROM users WHERE user_id = #{id}")
    void adminDelete(Integer id);
    
    // 用户注册
    @Insert("INSERT INTO users(username, password_hash, role_id, create_time, status) " +
            "VALUES (#{username}, #{password}, (SELECT role_id FROM roles WHERE role_name = 'user'), NOW(), 1)")
    void add(String username, String password);

    // 更新用户基本信息
    @Update("UPDATE users SET nickname = #{nickname}, email = #{email} WHERE user_id = #{id}")
    void update(User user);

    // 更新用户头像
    @Update("UPDATE users SET user_pic = #{avatarUrl} WHERE user_id = #{id}")
    void updateAvatar(String avatarUrl, Integer id);

    // 更新用户密码
    @Update("UPDATE users SET password_hash = #{md5String} WHERE user_id = #{id}")
    void updatePwd(String md5String, Integer id);

    // 增加用户下载次数
    @Update("UPDATE users SET download_count = download_count + #{delta} WHERE user_id = #{id}")
    void incrSumDownloadBy(@Param("id") Integer id, @Param("delta") Integer delta);
    
    // 更新最后登录时间
    @Update("UPDATE users SET last_login_time = NOW() WHERE user_id = #{id}")
    void updateLastLoginTime(Integer id);
}
