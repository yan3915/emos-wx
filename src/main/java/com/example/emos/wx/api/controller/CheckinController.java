package com.example.emos.wx.api.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.example.emos.wx.api.EmosException.EmosException;
import com.example.emos.wx.api.form.Checkinfrom;
import com.example.emos.wx.api.service.CheckinService;
import com.example.emos.wx.api.shiro.JwtUtil;
import com.example.emos.wx.api.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

@RequestMapping("/checkin")
@RestController
@Api("签到模块的web借口")
@Slf4j
public class CheckinController {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CheckinService checkinService;

    @Value("${emos.image-folder}")
    private String imageFolder;

    @GetMapping("/validCanChenkIn")
    @ApiOperation("查看用户今天可以签到")
    public R validCanCheckIn(@RequestHeader("token") String token){

        int userId = jwtUtil.getUserId(token);

        String result = checkinService.validCanCheckIn(userId, DateUtil.today());

        return R.ok(result);
    }

    @PostMapping("/checkin")
    @ApiOperation("签到")
    public R checkIn(@Valid Checkinfrom from, @RequestParam("photo") MultipartFile file, @RequestHeader("token") String token){
      if(file==null){
          return R.error("没有上传文件");
      }
      int userId = jwtUtil.getUserId(token);
      String filename = file.getOriginalFilename().toLowerCase();
      if(!filename.endsWith(".jpg"))
      {
          return R.error("必须提交jpg图片");
      }
      else {
          String path = imageFolder+ "/" +filename;
          try{
          file.transferTo(Paths.get(path));
              HashMap param =new HashMap();
              param.put("userId",userId);
              param.put("path",path);
              param.put("city",from.getCity());
              param.put("address",from.getAddress());
              param.put("country",from.getCountry());
              param.put("province",from.getProvince());
              param.put("district",from.getDistrict());
              checkinService.checkin(param);
              return R.ok("签到成功");
          }
          catch (IOException e){
              log.error(e.getMessage(),e);
              throw new EmosException("图片保存失败");
          }
          finally {
              FileUtil.del(path);
          }
      }
   }
   @PostMapping("createFaceModel")
   public R createFaceModel(@RequestParam("photo") MultipartFile file,@RequestHeader("token") String token){
       if(file==null){
           return R.error("没有上传文件");
       }
       int userId = jwtUtil.getUserId(token);
       String filename = file.getOriginalFilename().toLowerCase();
       if(!filename.endsWith(".jpg"))
       {
           return R.error("必须提交jpg图片");
       }
       else {
           String path = imageFolder+ "/" +filename;
           try{
               file.transferTo(Paths.get(path));
               checkinService.createFaceModel(userId,path);
               return R.ok("人脸建模成功");
           }
           catch (IOException e){
               log.error(e.getMessage(),e);
               throw new EmosException("图片保存失败");
           }
           finally {
               FileUtil.del(path);
           }
       }
   }
    
}
