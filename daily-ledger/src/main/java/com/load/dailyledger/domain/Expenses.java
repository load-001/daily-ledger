package com.load.dailyledger.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 支出流水
 * @TableName expenses
 */
@TableName(value ="expenses")
@Data
public class Expenses {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *
     */
    private Long userId;

    /**
     *
     */
    private Long categoryId;

    /**
     * 金额，单位元
     */
    private BigDecimal amount;

    /**
     * 消费日期
     */
    private Date expenseDate;

    /**
     *
     */
    private String note;

    /**
     *
     */
    private Date createdAt;

    /**
     *
     */
    private Date updatedAt;

    /**
     * 软删除时间
     */
    private Date deletedAt;
}
