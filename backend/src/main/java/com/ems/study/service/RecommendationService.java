package com.ems.study.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ems.study.entity.Recommendation;
import com.ems.study.mapper.RecommendationMapper;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService extends ServiceImpl<RecommendationMapper, Recommendation> {
}
