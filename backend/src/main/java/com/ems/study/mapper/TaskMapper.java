package com.ems.study.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ems.study.entity.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
