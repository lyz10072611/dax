package com.lyz.pojo;

import com.lyz.anno.State;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
@Data
public class Flow {
    private Integer id;
    @NotEmpty
    @Pattern(regexp ="^\\S{1,10}$")
    private String title;//标题
    @NotEmpty
    private String content;//内容
    @URL
    @NotEmpty
    private String coverImg;//封面
    @State
    private String state;//发布状态  已发布|草稿
    private Integer createUser;//创建人ID
    private LocalDateTime createTime;
    @NotNull
    private Integer categoryId ;//文章分类id
    private LocalDateTime updateTime;//更新时间
}
