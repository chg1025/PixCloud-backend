package com.chg.pixCloud.model.vo.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间使用分析响应
 */
@Data
public class SpaceUsageAnalyzeResponse implements Serializable {

    private static final long serialVersionUID = -5011960235621950614L;

    /**
     * 已使用大小
     */
    private Long usedSize;

    /**
     * 总大小
     */
    private Long maxSize;

    /**
     * 空间使用比例
     */
    private Double sizeUsageRatio;

    /**
     * 当前图片数量
     */
    private Long usedCount;

    /**
     * 最大图片数量
     */
    private Long maxCount;

    /**
     * 图片数量占比
     */
    private Double countUsageRatio;

    public SpaceUsageAnalyzeResponse(long useSize, long useCount) {
        this.usedSize = useSize;
        this.usedCount = useCount;
        this.maxCount = null;
        this.countUsageRatio = null;
        this.maxSize = null;
        this.sizeUsageRatio = null;
    }

    public SpaceUsageAnalyzeResponse(Long totalSize, Long totalCount, Long maxSize, Long maxCount, double sizeUsageRatio, double countUsageRatio) {
        this.usedSize = totalSize;
        this.usedCount = totalCount;
        this.maxSize = maxSize;
        this.countUsageRatio = countUsageRatio;
        this.maxCount = maxCount;
        this.sizeUsageRatio = sizeUsageRatio;
    }
}
