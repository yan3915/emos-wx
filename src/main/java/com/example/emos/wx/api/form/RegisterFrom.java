package com.example.emos.wx.api.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
@Data
@ApiModel
public class RegisterFrom {
    @NotBlank(message = "注册码不能为空")
    @Pattern(regexp = "^[0-9]{6}$",message = "注册码必须为六个数字")
    private String registerCode;

    @NotBlank(message = "微信临时授权不能为空")
    private  String code;

    @NotBlank(message = "昵称不能为空")
    private  String nickname;

    @NotBlank(message = "头像不能为空")
    private String photo;
}
