package com.example.emos.wx.api.db.dao;


import com.example.emos.wx.api.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
@Mapper
public interface TbCheckinDao {

  public  Integer  haveCheckin(HashMap param);

  public void insert(TbCheckin checkin);
}