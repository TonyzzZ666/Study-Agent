package com.ems.countdown.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ems.common.utils.ResultUtil;
import com.ems.countdown.entity.Countdown;
import com.ems.countdown.service.CountdownService;
import com.ems.study.entity.Task;
import com.ems.study.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/countdown")
public class CountdownController extends ResultUtil {

    private final CountdownService service;
    private final TaskService taskService;

    private Long uid() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return Long.valueOf(((User) a.getPrincipal()).getUsername());
    }

    @GetMapping
    public ResponseEntity<Object> list() {
        Long userId = uid();
        List<Countdown> all = service.list(
            new LambdaQueryWrapper<Countdown>().eq(Countdown::getUserId, userId).orderByAsc(Countdown::getTargetDate));
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Countdown c : all) {
            long days = today.until(c.getTargetDate()).getDays();
            if (days < 0) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId()); item.put("title", c.getTitle());
            item.put("targetDate", c.getTargetDate().toString());
            item.put("daysLeft", days);
            result.add(item);
        }
        return success(result);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Map<String, String> body) {
        Countdown c = new Countdown();
        c.setUserId(uid()); c.setTitle(body.get("title"));
        c.setTargetDate(LocalDate.parse(body.get("targetDate")));
        service.save(c); return success(c);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        service.removeById(id); return success("已删除");
    }

    /** 可导入的任务列表（无需打卡、未逾期、未在倒数日中） */
    @GetMapping("/available-tasks")
    public ResponseEntity<Object> availableTasks() {
        Long userId = uid();
        LocalDate today = LocalDate.now();
        // 获取已有的倒数目标题
        Set<String> existing = service.list(
            new LambdaQueryWrapper<Countdown>().eq(Countdown::getUserId, userId))
            .stream().map(Countdown::getTitle).collect(Collectors.toSet());

        List<Task> tasks = taskService.listByUser(userId, null, null).stream()
            .filter(t -> t.getDeadline() != null && !t.getDeadline().toLocalDate().isBefore(today))
            .filter(t -> ("NONE".equals(t.getCheckinType()) || t.getCheckinType() == null))
            .filter(t -> !existing.contains(t.getTitle()))
            .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Task t : tasks) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", t.getId()); item.put("title", t.getTitle());
            item.put("deadline", t.getDeadline().toString());
            item.put("priority", t.getPriority());
            result.add(item);
        }
        return success(result);
    }
}
