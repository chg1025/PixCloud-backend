package com.chg.pixCloud.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 空间级别信息
 */
@Data
@AllArgsConstructor
public class SpaceLevel {

    /**
     * 空间级别
     */
    private int value;

    /**
     * 空间名称
     */
    private String text;

    /**
     * 最大数量
     */
    private long maxCount;

    /**
     * 最大容量
     */
    private long maxSize;
}
