package com.chg.pixCloud.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间使用排行分析
 */
@Data
public class SpaceRankAnalyzeRequest implements Serializable {

    private static final long serialVersionUID = -559119458051217780L;

    /**
     * 排名前 N 的空间（默认前 10 ）
     */
    private Integer topN = 10;
}
