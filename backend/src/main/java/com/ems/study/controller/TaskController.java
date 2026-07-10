package com.ems.study.controller;

import com.ems.common.utils.ResultUtil;
import com.ems.study.entity.Task;
import com.ems.study.service.TaskService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController extends ResultUtil {

    private final TaskService taskService;

    /** 获取当前用户ID */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.valueOf(((User) auth.getPrincipal()).getUsername());
    }

    /** 任务列表（支持筛选） */
    @GetMapping
    public ResponseEntity<Object> list(@RequestParam(required = false) String status,
                                        @RequestParam(required = false) String priority) {
        List<Task> tasks = taskService.listByUser(getCurrentUserId(), status, priority);
        return success(tasks);
    }

    /** 创建任务 */
    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Task task) {
        task.setUserId(getCurrentUserId());
        task.setStatus("TODO");
        if (task.getNeedReview() == null) task.setNeedReview(false);
        if (task.getDeadline() == null) task.setDeadline(LocalDateTime.now().plusDays(7));
        taskService.save(task);
        return success(task);
    }

    /** 编辑任务 */
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        task.setUserId(getCurrentUserId());
        taskService.updateById(task);
        return success(task);
    }

    /** 删除任务 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        taskService.removeById(id);
        return success("删除成功");
    }

    /** 即将到期的任务（提醒用） */
    @GetMapping("/upcoming")
    public ResponseEntity<Object> upcoming() {
        return success(taskService.getUpcoming(getCurrentUserId()));
    }

    /** 今日需打卡的任务 */
    @GetMapping("/today-checkin")
    public ResponseEntity<Object> todayCheckin() {
        return success(taskService.getTodayCheckinTasks(getCurrentUserId()));
    }

    /** 今日需完成的任务（截止日期今天+无需打卡） */
    @GetMapping("/today-complete")
    public ResponseEntity<Object> todayComplete() {
        Long userId = getCurrentUserId();
        LocalDate today = LocalDate.now();
        List<Task> all = taskService.listByUser(userId, null, null);
        List<Task> result = all.stream()
            .filter(t -> t.getDeadline() != null)
            .filter(t -> t.getDeadline().toLocalDate().equals(today))
            .filter(t -> "NONE".equals(t.getCheckinType()) || t.getCheckinType() == null)
            .collect(Collectors.toList());
        return success(result);
    }
}
