package com.example.emos.wx.api.service;

import java.util.HashMap;

public interface CheckinService {

    public String validCanCheckIn(int userId,String date);

    public String checkin(HashMap param);
}
