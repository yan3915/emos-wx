package com.example.emos.wx.api.db.dao;
import com.example.emos.wx.api.db.pojo.TbHolidays;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbHolidaysDao {

    public Integer searchTodayIsHolidays();
}