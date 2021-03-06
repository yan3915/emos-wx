package com.example.emos.wx.api.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.example.emos.wx.api.EmosException.EmosException;
import com.example.emos.wx.api.config.SystemConstants;
import com.example.emos.wx.api.db.dao.*;
import com.example.emos.wx.api.db.pojo.TbCheckin;
import com.example.emos.wx.api.db.pojo.TbFaceModel;
import com.example.emos.wx.api.service.CheckinService;

import com.example.emos.wx.api.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.message.SimpleMessage;
import org.jsoup.Jsoup;
import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.io.File;
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

    @Autowired
    private TbCityDao tbCityDao;

    @Autowired
    private EmailTask emailTask;

    @Autowired
    private TbUserDao userDao;

    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;

    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;

    @Value("${emos.email.hr}")
    private String hrEmail;



   //@Value("${emos.face.createFaceModelUrl}")
   // private String createFaceModelUrl;

   // @Value("${emos.face.checkinUrl}")
   // private String checkinUrl;

    @Override
    public String validCanCheckIn(int userId, String date) {
        boolean bool_1 = holidaysDao.searchTodayIsHolidays() != null ? true : false;
        boolean bool_2 = workdayDao.searchTodayIsWorkday() != null ? true : false;
        String type = "?????????";
        if (DateUtil.date().isWeekend()) {
            type = "?????????";
        }
        if (bool_1) {
            type = "?????????";
        } else if (bool_2) {
            type = "?????????";
        }

        if (type.equals("?????????")) {
            return "????????????????????????";
        } else {
            DateTime now = DateUtil.date();
            String start = DateUtil.today() + " " + constants.attendanceStartTime;
            String end = DateUtil.today() + " " + constants.attendanceEndTime;
            DateTime attendanceStart = DateUtil.parse(start);
            DateTime attendanceEnd = DateUtil.parse(end);
            if(now.isBefore(attendanceStart)){
                return "??????????????????????????????";
            }
            else if(now.isAfter(attendanceEnd)){
                return "?????????????????????????????????";
            }else {
                HashMap map=new HashMap();
                map.put("userId",userId);
                map.put("date",date);
                map.put("start",start);
                map.put("end",end);
                boolean bool=tbCheckinDao.haveCheckin(map)!=null?true:false;
                return bool?"???????????????????????????????????????" : "????????????";
            }
        }
    }

    @Override
    public void checkin(HashMap param) {

        //????????? ??????????????????
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
            throw new EmosException("?????????????????????");
        }
        else {
            String path=(String) param.get("path");
            HttpRequest request = HttpUtil.createPost(checkinUrl);
            request.form("photo", FileUtil.file(path),"targetModel");
            HttpResponse response = request.execute();
            if(response.getStatus()!=200){
                log.error("??????????????????");
                throw new EmosException("??????????????????");
            }
            String body = response.body();
            if("?????????????????????".equals(body)||"???????????????????????????".equals(body)){
               throw new EmosException(body);
            }
            else if("Flase".equals(body)){
                throw new EmosException("??????????????????????????????");
            }
            else if ("True".equals(body)){
                int risk = 1; //??????????????????
                String address =  (String)param.get("address");
                String country =  (String) param.get("country");
                String province = (String) param.get("province");
                //??????????????????
                String city = (String) param.get("city");
                String district = (String) param.get("district");
                if(!StrUtil.isBlank(city) && !StrUtil.isBlank(district)){
                   String code = tbCityDao.searchCode(city);
                   try {
                       String url = "http://m." + code + ".bendibao.com/news/yqdengji/?qu=" +district;
                    Document document = Jsoup.connect(url).get();
                    Elements elements= document.getElementsByClass("list-context");
                    if(elements.size()>0){
                       Element element =  elements.get(0);
                       String result = element.select("p:last-child").text();
                       if("?????????".equals(result)){
                          risk = 3;
                          //Todo ??????????????????
                          HashMap<String,String> map = userDao.searchNameAndDept(userId);
                          String name = map.get("name");
                          String deptName = map.get("dept_name");
                          deptName = deptName!= null?deptName:"";
                           SimpleMailMessage message = new SimpleMailMessage();
                           message.setTo(hrEmail);
                           message.setText(deptName +"??????" +name +"," + DateUtil.format(new Date(),"yyyy???MM???dd???")+ "??????" + address + ",??????????????????????????????????????????????????????????????????????????????");
                       }
                       else if("?????????".equals(result)){
                           risk = 2;
                       }

                    }
                   }catch (Exception e){
                       throw new EmosException("??????????????????????????????");
                   }
                }
                //TODO ?????????????????????
                TbCheckin entity = new TbCheckin();
                entity.setUserId(userId);
                entity.setAddress(address);
                entity.setCountry(country);
                entity.setProvince(province);
                entity.setCity(city);
                entity.setDistrict(district);
                entity.setStatus((byte) status);
                entity.setDate(DateUtil.today());
                entity.setCreateTime(d1);
                tbCheckinDao.insert(entity);
            }
        }
    }

    @Override
    public void createFaceModel(int userId, String path) {
      HttpRequest request =  HttpUtil.createPost(createFaceModelUrl);
      request.form("photo", FileUtil.file(path));
      HttpResponse response = request.execute();
      String body = response.body();
      if("?????????????????????".equals(body) || "???????????????????????????".equals(body)){
          throw new EmosException(body);
      }
      else {
          TbFaceModel entity = new TbFaceModel();
          entity.setUserId(userId);
          entity.setFaceModel(body);
          tbFaceModelDao.insert(entity);
      }
    }
}
