package com.chg.pixCloud.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    private static final long serialVersionUID = 4577072499214994891L;
    /**
     * 图片id（用户修改）
     */
    private Long id;
    /**
     * 图片地址（url）
     */
    private String url;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 空间id
     */
    private Long spaceId;
}
