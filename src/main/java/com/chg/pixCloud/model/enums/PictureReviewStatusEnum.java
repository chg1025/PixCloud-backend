package com.chg.pixCloud.model.enums;

import lombok.Getter;

/**
 * @program: pixCloud_backend
 * @description: 图片审核枚举
 * @author: chg
 * @create: 2024-12-31 21:29
 **/
@Getter
public enum PictureReviewStatusEnum {
    REVIEWING("待审核", 0),
    PASS("审核通过", 1),
    REJECT("拒绝", 2);

    private final String text;
    private final int value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value值获取枚举
     *
     * @param value
     * @return 枚举值
     */
    public static PictureReviewStatusEnum getPictureReviewStatusEnumByValue(int value) {
        for (PictureReviewStatusEnum pictureReviewStatusEnum : PictureReviewStatusEnum.values()) {
            if (pictureReviewStatusEnum.getValue() == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }
}
