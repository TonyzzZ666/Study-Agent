package com.ems.countdown.service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ems.countdown.entity.Countdown;
import com.ems.countdown.mapper.CountdownMapper;
import org.springframework.stereotype.Service;
@Service public class CountdownService extends ServiceImpl<CountdownMapper, Countdown> {}
