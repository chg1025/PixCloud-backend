package com.chg.pixCloud.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量修改信息请求
 */
@Data
public class PictureEditByBatchRequest implements Serializable {

    private static final long serialVersionUID = 5639871846442461433L;

    /**
     * 图片 id 列表
     */
    private List<Long> pictureIdList;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 命名规则
     */
    private String nameRule;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;
}
