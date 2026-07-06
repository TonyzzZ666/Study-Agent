package com.ems.study.controller;

import com.ems.common.utils.ResultUtil;
import com.ems.study.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recommendations")
public class RecommendationController extends ResultUtil {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<Object> listAll() {
        return success(recommendationService.list());
    }
}
