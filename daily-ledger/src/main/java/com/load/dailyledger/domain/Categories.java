package com.load.dailyledger.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 支出分类
 * @TableName categories
 */
@TableName(value ="categories")
@Data
public class Categories {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 0 表示系统公共分类
     */
    private Long userId;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private String icon;

    /**
     *
     */
    private int sortOrder;

    /**
     *
     */
    private Date createdAt;


}
