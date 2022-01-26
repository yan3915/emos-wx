package com.example.emos.wx.api.db.dao;
import com.example.emos.wx.api.db.pojo.TbWorkday;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbWorkdayDao {
   public Integer searchTodayIsWorkday();
}