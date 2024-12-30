package com.chg.pixCloud.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片标签分类列表视图
 */
@Data
public class PictureTagCategory implements Serializable {

    private static final long serialVersionUID = 6502846378004240950L;

    /**
     * 标签列表
     */
    private List<String> tagList;
    /**
     * 分类列表
     */
    private List<String> categoryList;
}
