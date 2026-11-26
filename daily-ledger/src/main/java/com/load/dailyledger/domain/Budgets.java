package com.load.dailyledger.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 月度预算
 * @TableName budgets
 */
@TableName(value ="budgets")
@Data
public class Budgets {
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
     * 所在月 1 号
     */
    private Date budgetMonth;

    /**
     *
     */
    private BigDecimal amount;

    /**
     *
     */
    private Date createdAt;

    /**
     *
     */
    private Date updatedAt;

}
