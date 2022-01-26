package com.example.emos.wx.api.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.example.emos.wx.api.EmosException.EmosException;
import com.example.emos.wx.api.config.SystemConstants;
import com.example.emos.wx.api.db.dao.TbCheckinDao;
import com.example.emos.wx.api.db.dao.TbFaceModelDao;
import com.example.emos.wx.api.db.dao.TbHolidaysDao;
import com.example.emos.wx.api.db.dao.TbWorkdayDao;
import com.example.emos.wx.api.db.pojo.TbCheckin;
import com.example.emos.wx.api.service.CheckinService;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.DataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;

@Service
@Scope("prototype")
@Slf4j
public class CheckinServiceImpl implements CheckinService {

    @Autowired
    private SystemConstants constants;

    @Autowired
    private TbHolidaysDao holidaysDao;

    @Autowired
    private TbWorkdayDao workdayDao;

    @Autowired
    private TbCheckinDao tbCheckinDao;

    @Autowired
    private TbFaceModelDao tbFaceModelDao;

    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;

    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;

    @Override
    public String validCanCheckIn(int userId, String date) {
        boolean bool_1 = holidaysDao.searchTodayIsHolidays() != null ? true : false;
        boolean bool_2 = workdayDao.searchTodayIsWorkday() != null ? true : false;
        String type = "工作日";
        if (DateUtil.date().isWeekend()) {
            type = "节假日";
        }
        if (bool_1) {
            type = "节假日";
        } else if (bool_2) {
            type = "工作日";
        }

        if (type.equals("节假日")) {
            return "节假日不需要考勤";
        } else {
            DateTime now = DateUtil.date();
            String start = DateUtil.today() + " " + constants.attendanceStartTime;
            String end = DateUtil.today() + " " + constants.attendanceEndTime;
            DateTime attendanceStart = DateUtil.parse(start);
            DateTime attendanceEnd = DateUtil.parse(end);
            if(now.isBefore(attendanceStart)){
                return "没到上班考勤开始时间";
            }
            else if(now.isAfter(attendanceEnd)){
                return "超过了上班考勤结束时间";
            }else {
                HashMap map=new HashMap();
                map.put("userId",userId);
                map.put("date",date);
                map.put("start",start);
                map.put("end",end);
                boolean bool=tbCheckinDao.haveCheckin(map)!=null?true:false;
                return bool?"今日已经考勤，不用重复考勤" : "可以考勤";
            }
        }
    }

    @Override
    public String checkin(HashMap param) {

        //区时间 判断是否迟到
        Date d1 = DateUtil.date();
        Date d2 = DateUtil.parse(DateUtil.today()+""+constants.attendanceTime);
        Date d3 = DateUtil.parse(DateUtil.today()+""+constants.attendanceEndTime);
        int status=1;
        if(d1.compareTo(d2)<=0){
            status=1;
        }
        else if((d1.compareTo(d2)>0) && (d1.compareTo(d3))<0) {
            status=2;
        }
       int userId=(Integer) param.get("userId");
        String faceModel = tbFaceModelDao.searchFaceModel(userId);
        if(faceModel==null){
            throw new EmosException("不存在人脸模型");
        }
        else {
            String path=(String) param.get("path");
            HttpRequest request = HttpUtil.createPost(checkinUrl);
            request.form("photo", FileUtil.file(path),"targetModel");
            HttpResponse response = request.execute();
            if(response.getStatus()!=200){
                log.error("人脸服务异常");
                throw new EmosException("人脸服务异常");
            }
            String body = response.body();
            if("无法识别出人脸".equals(body)||"照片中存在多张人脸".equals(body)){
               throw new EmosException(body);
            }
            else if("Flase".equals(body)){
                throw new EmosException("签到无效，非本人签到");
            }
            else if ("True".equals(body)){
                // todo 查询疫情风险等级
                // todo 保存签到记录
            }
        }
    }
}
