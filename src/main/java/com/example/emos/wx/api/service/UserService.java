package com.example.emos.wx.api.service;

import com.example.emos.wx.api.db.pojo.TbUser;

import java.util.Set;

public interface UserService {
    public int registerUser(String registerCode,String code,String nickname,String photo);

    public Set<String> searchUserPermissions(int userId);

    public TbUser searchById(int userId);

    public  Integer login(String code);
}
