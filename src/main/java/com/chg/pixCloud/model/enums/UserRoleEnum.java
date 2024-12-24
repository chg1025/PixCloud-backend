package com.chg.pixCloud.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @program: pixCloud_backend
 * @description: 用户角色枚举
 * @author: chg
 * @create: 2024-12-24 21:29
 **/
@Getter
public enum UserRoleEnum {
    USER("用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value值获取枚举
     *
     * @param value
     * @return 枚举值
     */
    public static UserRoleEnum getUserRoleEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.getValue().equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }
}
