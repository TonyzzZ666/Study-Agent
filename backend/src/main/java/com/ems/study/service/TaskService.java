package com.ems.study.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ems.study.entity.Task;
import com.ems.study.mapper.TaskMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService extends ServiceImpl<TaskMapper, Task> {

    public List<Task> listByUser(Long userId, String status, String priority) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Task::getStatus, status);
        }
        if (priority != null && !priority.isEmpty()) {
            wrapper.eq(Task::getPriority, priority);
        }
        wrapper.orderByDesc(Task::getPriority).orderByAsc(Task::getDeadline);
        return list(wrapper);
    }

    /** 即将到期的任务（3天内 + 按优先级排序） */
    public List<Task> getUpcoming(Long userId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId)
               .eq(Task::getStatus, "TODO")
               .apply("deadline <= DATE_ADD(NOW(), INTERVAL 3 DAY)")
               .orderByDesc(Task::getPriority);
        return list(wrapper);
    }
}
