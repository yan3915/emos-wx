package com.example.emos.wx.api.db.dao;

import com.example.emos.wx.api.db.pojo.TbFaceModel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbFaceModelDao {

    public  int  deleteFaceModel(int userId);

    public void insert(TbFaceModel faceModelEntity);

    public String searchFaceModel(int userId);
}