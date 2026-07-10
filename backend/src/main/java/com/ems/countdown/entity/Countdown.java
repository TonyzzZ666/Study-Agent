package com.ems.countdown.entity;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate; import java.time.LocalDateTime;
@TableName("countdown")
public class Countdown {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId; private String title; private LocalDate targetDate; private LocalDateTime createdAt;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public LocalDate getTargetDate(){return targetDate;} public void setTargetDate(LocalDate v){targetDate=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
