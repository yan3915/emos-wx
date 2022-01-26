package com.example.emos.wx.api.db.dao;
import com.example.emos.wx.api.db.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.Set;

@Mapper
public interface TbUserDao {
    public boolean haveRootUser();

    public  Integer searchIdOpenId( String openId);

    public int insert(HashMap param);

    public Set<String> searchUserPermissions(int userId);

    public  Integer searchIdByOpenId(String openId);

    public TbUser searchById(int userId);

}