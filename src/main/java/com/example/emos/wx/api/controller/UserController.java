package com.example.emos.wx.api.controller;

import com.example.emos.wx.api.form.Loginform;
import com.example.emos.wx.api.form.RegisterFrom;
import com.example.emos.wx.api.service.UserService;
import com.example.emos.wx.api.shiro.JwtUtil;
import com.example.emos.wx.api.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Api("用户模块Web接口")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    //redis缓存令牌
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    /**
    @Value("${trtc.appid}")
    private Integer appid;

    @Value("${trtc.key}")
    private String key;

    @Value("${trtc.expire}")
    private Integer expire;
*/
    @PostMapping("/register")
    @ApiOperation("注册用户")
    public R register(@Valid @RequestBody RegisterFrom form){
        int id=   userService.registerUser(form.getRegisterCode(),form.getCode(),form.getNickname(),form.getPhoto());
        String token =jwtUtil.createToken(id); //创建令牌字符串
        Set<String> permisSet=userService.searchUserPermissions(id); //用户的权限
        saveCacheToken(token,id);
        return R.ok("用户注册成功").put("token",token).put("permission",permisSet);

    }
    private void saveCacheToken(String token, int userId){
        redisTemplate.opsForValue().set(token,userId+"",cacheExpire, TimeUnit.DAYS);
    }
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public R login(@Valid @RequestBody Loginform from){
        int id=userService.login(from.getCode());
        String token =jwtUtil.createToken(id);
        saveCacheToken(token,id);
        Set<String> permsSet= userService.searchUserPermissions(id);
        return R.ok("登陆成功").put("token",token).put("permission",permsSet);
    }
}
