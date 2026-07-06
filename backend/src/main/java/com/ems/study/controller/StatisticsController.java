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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticsController extends ResultUtil {

    private final TaskService taskService;
    private final CheckInService checkInService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.valueOf(((User) auth.getPrincipal()).getUsername());
    }

    @GetMapping
    public ResponseEntity<Object> getStatistics() {
        Long userId = getCurrentUserId();

        // 总任务数 & 完成数
        List<Task> allTasks = taskService.listByUser(userId, null, null);
        long total = allTasks.size();
        long done = allTasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();

        // 完成率
        double rate = total > 0 ? (double) done / total * 100 : 0;

        // 打卡天数
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getUserId, userId);
        long checkDays = checkInService.list(wrapper).stream()
                .map(c -> c.getCheckTime().toLocalDate())
                .distinct().count();

        // 每日任务添加数 & 完成数（最近7天）
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString();
            long created = allTasks.stream()
                    .filter(t -> t.getCreateTime() != null && t.getCreateTime().toLocalDate().equals(date))
                    .count();
            long completed = allTasks.stream()
                    .filter(t -> "DONE".equals(t.getStatus()))
                    .filter(t -> {
                        // 简单处理：看打卡记录中是否有当天的
                        return checkInService.list().stream().anyMatch(c ->
                            c.getTaskId().equals(t.getId()) && c.getCheckTime().toLocalDate().equals(date));
                    }).count();
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", dateStr);
            day.put("created", created);
            day.put("completed", completed);
            dailyStats.add(day);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("done", done);
        result.put("rate", Math.round(rate * 10) / 10.0);
        result.put("checkDays", checkDays);
        result.put("dailyStats", dailyStats);

        return success(result);
    }
}
