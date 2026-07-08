package com.ems.study.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ems.common.utils.ResultUtil;
import com.ems.study.entity.CheckIn;
import com.ems.study.entity.Task;
import com.ems.study.service.CheckInService;
import com.ems.study.service.TaskService;
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
@RequestMapping("/checkin")
public class CheckInController extends ResultUtil {

    private final CheckInService checkInService;
    private final TaskService taskService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.valueOf(((User) auth.getPrincipal()).getUsername());
    }

    /** 每日打卡（父子联动，可撤销） */
    @PostMapping("/{taskId}")
    public ResponseEntity<Object> doCheckIn(@PathVariable Long taskId) {
        Long userId = getCurrentUserId();
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = start.plusDays(1);

        // 查今天是否已打卡
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getUserId, userId)
               .eq(CheckIn::getTaskId, taskId)
               .between(CheckIn::getCheckTime, start, end)
               .last("LIMIT 1");
        CheckIn exist = checkInService.getOne(wrapper);

        if (exist != null) {
            // 撤销打卡
            checkInService.removeById(exist.getId());
            // 父任务撤销：同时撤销所有子任务
            Task task = taskService.getById(taskId);
            if (task != null && task.getParentId() == null) {
                List<Task> subs = taskService.list(new LambdaQueryWrapper<Task>().eq(Task::getParentId, taskId));
                subs.forEach(s -> {
                    LambdaQueryWrapper<CheckIn> sw = new LambdaQueryWrapper<>();
                    sw.eq(CheckIn::getUserId, userId).eq(CheckIn::getTaskId, s.getId())
                      .between(CheckIn::getCheckTime, start, end).last("LIMIT 1");
                    CheckIn se = checkInService.getOne(sw);
                    if (se != null) checkInService.removeById(se.getId());
                });
            }
            return success("已取消打卡");
        }

        // 打卡
        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setTaskId(taskId);
        checkIn.setCheckTime(LocalDateTime.now());
        checkInService.save(checkIn);

        // 父任务打卡：联动所有子任务
        Task task = taskService.getById(taskId);
        if (task != null && task.getParentId() == null) {
            List<Task> subs = taskService.list(new LambdaQueryWrapper<Task>().eq(Task::getParentId, taskId));
            subs.forEach(s -> {
                LambdaQueryWrapper<CheckIn> sw = new LambdaQueryWrapper<>();
                sw.eq(CheckIn::getUserId, userId).eq(CheckIn::getTaskId, s.getId())
                  .between(CheckIn::getCheckTime, start, end).last("LIMIT 1");
                if (checkInService.getOne(sw) == null) {
                    CheckIn sc = new CheckIn();
                    sc.setUserId(userId); sc.setTaskId(s.getId());
                    sc.setCheckTime(LocalDateTime.now());
                    checkInService.save(sc);
                }
            });
        }
        // 子任务打卡：检查兄弟是否全打卡，联动父任务
        if (task != null && task.getParentId() != null) {
            List<Task> siblings = taskService.list(new LambdaQueryWrapper<Task>().eq(Task::getParentId, task.getParentId()));
            boolean allChecked = siblings.stream().allMatch(s -> {
                LambdaQueryWrapper<CheckIn> sw = new LambdaQueryWrapper<>();
                sw.eq(CheckIn::getUserId, userId).eq(CheckIn::getTaskId, s.getId())
                  .between(CheckIn::getCheckTime, start, end).last("LIMIT 1");
                return checkInService.getOne(sw) != null;
            });
            if (allChecked) {
                LambdaQueryWrapper<CheckIn> pw = new LambdaQueryWrapper<>();
                pw.eq(CheckIn::getUserId, userId).eq(CheckIn::getTaskId, task.getParentId())
                  .between(CheckIn::getCheckTime, start, end).last("LIMIT 1");
                if (checkInService.getOne(pw) == null) {
                    CheckIn pc = new CheckIn();
                    pc.setUserId(userId); pc.setTaskId(task.getParentId());
                    pc.setCheckTime(LocalDateTime.now());
                    checkInService.save(pc);
                }
            }
        }
        return success("打卡成功");
    }

    /** 完成任务（可撤销，父子联动） */
    @PutMapping("/complete/{taskId}")
    public ResponseEntity<Object> completeTask(@PathVariable Long taskId) {
        Task task = taskService.getById(taskId);
        if (task == null) return fail("任务不存在");
        String newStatus = "DONE".equals(task.getStatus()) ? "TODO" : "DONE";
        task.setStatus(newStatus);
        taskService.updateById(task);

        // 如果是父任务：联动所有子任务
        if (task.getParentId() == null) {
            LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Task::getParentId, taskId);
            List<Task> subs = taskService.list(wrapper);
            subs.forEach(s -> { s.setStatus(newStatus); taskService.updateById(s); });
        }
        // 如果是子任务：检查兄弟是否全完成，联动父任务
        if (task.getParentId() != null) {
            LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Task::getParentId, task.getParentId());
            List<Task> siblings = taskService.list(wrapper);
            boolean allDone = siblings.stream().allMatch(s -> "DONE".equals(s.getStatus()));
            Task parent = taskService.getById(task.getParentId());
            if (parent != null) {
                parent.setStatus(allDone ? "DONE" : "TODO");
                taskService.updateById(parent);
            }
        }
        return success("DONE".equals(newStatus) ? "任务已完成" : "已恢复为待完成");
    }

    /** 今日已打卡的任务ID列表 */
    @GetMapping("/today")
    public ResponseEntity<Object> today() {
        Long userId = getCurrentUserId();
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = start.plusDays(1);
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getUserId, userId)
               .between(CheckIn::getCheckTime, start, end);
        List<Long> ids = checkInService.list(wrapper).stream().map(CheckIn::getTaskId).toList();
        return success(ids);
    }

    /** 打卡历史 */
    @GetMapping("/history")
    public ResponseEntity<Object> history() {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getUserId, userId).orderByDesc(CheckIn::getCheckTime);
        return success(checkInService.list(wrapper));
    }

    /** 打卡日历数据 */
    @GetMapping("/calendar")
    public ResponseEntity<Object> calendar(@RequestParam int year, @RequestParam int month) {
        return success(checkInService.getCalendarData(getCurrentUserId(), year, month));
    }
}
