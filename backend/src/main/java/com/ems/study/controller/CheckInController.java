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

    /** 打卡 */
    @PostMapping("/{taskId}")
    public ResponseEntity<Object> doCheckIn(@PathVariable Long taskId) {
        Long userId = getCurrentUserId();
        // 记录打卡
        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setTaskId(taskId);
        checkIn.setCheckTime(LocalDateTime.now());
        checkInService.save(checkIn);
        // 更新任务状态为已完成
        Task task = taskService.getById(taskId);
        if (task != null) {
            task.setStatus("DONE");
            taskService.updateById(task);
        }
        return success("打卡成功");
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
