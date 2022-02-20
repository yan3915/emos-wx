package com.example.emos.wx.api.db.pojo;

import java.io.Serializable;
import lombok.Data;

/**
 * sys_config
 * @author 
 */
@Data
public class TbFaceModel implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    //用户
    private Integer userId;

    private String faceModel;

    private static final long serialVersionUID = 1L;
}