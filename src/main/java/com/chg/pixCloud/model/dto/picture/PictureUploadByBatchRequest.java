package com.chg.pixCloud.model.dto.picture;

import com.chg.pixCloud.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量导入图片请求
 */
@Data
public class PictureUploadByBatchRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = -2448640160890049289L;

    /**
     * 搜索词
     */
    private String q;

    /**
     * 图片名称前缀(不传默认为搜索词)
     */
    private String namePrefix;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;
}
