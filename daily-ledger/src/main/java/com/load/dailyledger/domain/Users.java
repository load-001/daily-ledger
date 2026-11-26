package com.load.dailyledger.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户
 * @TableName users
 */
@TableName(value ="users")
@Data
public class Users {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *
     */
    private String email;

    /**
     *
     */
    private String passwordHash;

    /**
     *
     */
    private String displayName;

    /**
     *
     */
    private Date createdAt;

    /**
     *
     */
    private Date updatedAt;

}
