package com.example.emos.wx.api.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class Checkinfrom {
    private String address;
    private String country;
    private String province;
    private String city;
    private String district;
}
