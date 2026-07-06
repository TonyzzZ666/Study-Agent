package com.ems.study.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ems.study.entity.CheckIn;
import com.ems.study.mapper.CheckInMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CheckInService extends ServiceImpl<CheckInMapper, CheckIn> {

    public List<Map<String, Object>> getCalendarData(Long userId, int year, int month) {
        return baseMapper.getCalendarData(userId, year, month);
    }
}
