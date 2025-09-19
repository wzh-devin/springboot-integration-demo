package com.devin.sso.entity;

import java.io.Serial;
import java.math.BigInteger;
import java.util.Date;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (User)实体类.
 *
 * @author joey
 */
@Data
@TableName(value = "tb_user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 916151117584670539L;

    /**
     * 用户id.
     */
    @TableField("id")
    private BigInteger id;

    /**
     * 用户sso_id.
     */
    @TableField("sso_id")
    private String ssoId;

    /**
     * 用户名.
     */
    @TableField("username")
    private String username;

    /**
     * 密码.
     */
    @TableField("password")
    private String password;

    /**
     * 创建时间.
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间.
     */
    @TableField("update_time")
    private Date updateTime;
}

