package com.example.emos.wx.api.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@ApiModel
@Data
public class Loginform {
    @NotBlank(message = "临时授权不能为空")
    private String code;
}
