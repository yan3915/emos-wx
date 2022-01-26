package com.example.emos.wx.api.controller;

import cn.hutool.core.date.DateUtil;
import com.example.emos.wx.api.service.CheckinService;
import com.example.emos.wx.api.shiro.JwtUtil;
import com.example.emos.wx.api.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/checkin")
@RestController
@Api("签到模块的web借口")
@Slf4j
public class CheckinController {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CheckinService checkinService;

    @GetMapping("/validCanChenkIn")
    @ApiOperation("查看用户今天可以签到")
    public R validCanCheckIn(@RequestHeader("token") String token){

        int userId = jwtUtil.getUserId(token);

        String result = checkinService.validCanCheckIn(userId, DateUtil.today());

        return R.ok(result);
    }
    
}