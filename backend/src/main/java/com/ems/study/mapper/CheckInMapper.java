package com.ems.study.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ems.study.entity.CheckIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {

    /** 打卡日历数据：返回 [{date: "2026-07-01", count: 3}, ...] */
    @Select("SELECT DATE(check_time) as date, COUNT(*) as count " +
            "FROM check_in WHERE user_id = #{userId} " +
            "AND YEAR(check_time) = #{year} AND MONTH(check_time) = #{month} " +
            "GROUP BY DATE(check_time)")
    List<Map<String, Object>> getCalendarData(Long userId, int year, int month);
}
