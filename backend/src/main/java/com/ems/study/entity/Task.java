package com.ems.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ems.common.utils.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@TableName("task")
public class Task extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String description;

    /** HIGH / MEDIUM / LOW */
    private String priority;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    /** 是否需要复习提醒 */
    private Boolean needReview;

    /** 父任务ID（子任务用） */
    private Long parentId;

    /** 打卡频率：DAILY/WEEKDAYS/NONE/CUSTOM */
    private String checkinType;

    /** CUSTOM模式的打卡日，逗号分隔 1-7 (1=周一) */
    private String checkinDays;

    /** TODO / DONE */
    private String status;
}
