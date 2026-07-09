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

    /** 即将到期的任务（只返回父任务；子任务到期则返回其父任务） */
    public List<Task> getUpcoming(Long userId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId)
               .eq(Task::getStatus, "TODO")
               .apply("deadline <= DATE_ADD(NOW(), INTERVAL 3 DAY)")
               .orderByAsc(Task::getDeadline);
        List<Task> raw = list(wrapper);
        // 去重并转为父任务
        java.util.Map<Long, Task> result = new java.util.LinkedHashMap<>();
        for (Task t : raw) {
            if (t.getParentId() != null) {
                Task parent = getById(t.getParentId());
                if (parent != null) result.put(parent.getId(), parent);
            } else {
                result.put(t.getId(), t);
            }
        }
        // 按截止时间再按优先级排序
        return result.values().stream()
            .sorted((a, b) -> {
                int d = a.getDeadline().compareTo(b.getDeadline());
                if (d != 0) return d;
                return priorityOrder(b.getPriority()) - priorityOrder(a.getPriority());
            }).toList();
    }

    private int priorityOrder(String p) {
        return "HIGH".equals(p) ? 2 : "MEDIUM".equals(p) ? 1 : 0;
    }

    /** 今日需要打卡的任务 */
    public List<Task> getTodayCheckinTasks(Long userId) {
        int dow = java.time.LocalDate.now().getDayOfWeek().getValue(); // 1=Mon, 7=Sun
        List<Task> all = listByUser(userId, null, null);
        java.util.Map<Long, Task> result = new java.util.LinkedHashMap<>();
        for (Task t : all) {
            if ("DONE".equals(t.getStatus())) continue;
            if (t.getParentId() != null) continue; // 只拿父任务
            if (needsCheckin(t, dow)) result.put(t.getId(), t);
        }
        return result.values().stream()
            .sorted((a, b) -> {
                int d = a.getDeadline().compareTo(b.getDeadline());
                if (d != 0) return d;
                return priorityOrder(b.getPriority()) - priorityOrder(a.getPriority());
            }).toList();
    }

    private boolean needsCheckin(Task t, int dow) {
        String type = t.getCheckinType();
        if (type == null || "DAILY".equals(type)) return true;
        if ("NONE".equals(type)) return false;
        if ("WEEKDAYS".equals(type)) return dow <= 5;
        if ("CUSTOM".equals(type) && t.getCheckinDays() != null) {
            return java.util.Arrays.asList(t.getCheckinDays().split(",")).contains(String.valueOf(dow));
        }
        return true;
    }
}
