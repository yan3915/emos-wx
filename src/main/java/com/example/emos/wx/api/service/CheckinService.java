package com.example.emos.wx.api.service;

import java.util.HashMap;

public interface CheckinService {

    public String validCanCheckIn(int userId,String date);

    public void checkin(HashMap param);

    public void createFaceModel(int userId, String path);
}
