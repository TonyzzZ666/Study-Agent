package com.ems.calendar.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ems.common.utils.ResultUtil;
import com.ems.study.entity.Task;
import com.ems.study.entity.CheckIn;
import com.ems.study.service.TaskService;
import com.ems.study.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/calendar")
public class CalendarController extends ResultUtil {

    private final TaskService taskService;
    private final CheckInService checkInService;

    private Long uid() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return Long.valueOf(((User) a.getPrincipal()).getUsername());
    }

    /** 判断任务在某天是否需要出现 */
    private boolean taskActiveOnDay(Task t, LocalDate day) {
        if (t.getDeadline() == null) return false;
        LocalDate dl = t.getDeadline().toLocalDate();
        String ct = t.getCheckinType() != null ? t.getCheckinType() : "DAILY";

        // 时间线任务（NONE）：只在截止日期出现
        if ("NONE".equals(ct)) {
            return day.equals(dl);
        }
        // 打卡任务：从创建日到截止日之间，按打卡频率出现
        LocalDate created = t.getCreateTime() != null ? t.getCreateTime().toLocalDate() : dl.minusDays(30);
        if (day.isBefore(created) || day.isAfter(dl)) return false;

        int dow = day.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
        if ("DAILY".equals(ct)) return true;
        if ("WEEKDAYS".equals(ct)) return dow <= 5;
        if ("CUSTOM".equals(ct) && t.getCheckinDays() != null) {
            return Arrays.asList(t.getCheckinDays().split(",")).contains(String.valueOf(dow));
        }
        return true; // default: DAILY
    }

    @GetMapping("/tasks")
    public ResponseEntity<Object> tasks(@RequestParam int year, @RequestParam int month) {
        Long userId = uid();
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        // 获取所有未完成任务（截止日在月内或之后）
        List<Task> all = taskService.listByUser(userId, null, null).stream()
            .filter(t -> t.getDeadline() != null)
            .filter(t -> !t.getDeadline().toLocalDate().isBefore(start.minusDays(90)))
            .collect(Collectors.toList());

        List<CheckIn> checkins = checkInService.list(
            new LambdaQueryWrapper<CheckIn>().eq(CheckIn::getUserId, userId)
                .between(CheckIn::getCheckTime, start.atStartOfDay(), end.plusDays(1).atStartOfDay()));
        Map<Long, Set<String>> cdMap = new HashMap<>();
        for (CheckIn c : checkins) {
            cdMap.computeIfAbsent(c.getTaskId(), k -> new HashSet<>())
                .add(c.getCheckTime().toLocalDate().toString());
        }
        String today = LocalDate.now().toString();

        Map<String, List<Map<String, Object>>> byDate = new LinkedHashMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            byDate.put(d.toString(), new ArrayList<>());
        }

        for (Task t : all) {
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                if (taskActiveOnDay(t, d)) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", t.getId()); item.put("title", t.getTitle());
                    item.put("description", t.getDescription());
                    item.put("priority", t.getPriority()); item.put("status", t.getStatus());
                    item.put("deadline", t.getDeadline().toString());
                    item.put("parentId", t.getParentId());
                    item.put("checkinType", t.getCheckinType());
                    item.put("checkinDays", t.getCheckinDays());
                    Set<String> cd = cdMap.getOrDefault(t.getId(), Collections.emptySet());
                    item.put("checkedToday", cd.contains(d.toString()));
                    byDate.get(d.toString()).add(item);
                }
            }
        }
        return success(byDate);
    }
}
