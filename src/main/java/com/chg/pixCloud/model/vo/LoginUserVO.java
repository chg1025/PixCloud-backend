package com.chg.pixCloud.model.vo;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author chg
 * @description 已登录用户视图（脱敏）
 * @createDate 2024-12-24 22:15:56
 */
@Data
public class LoginUserVO implements Serializable {

    private static final long serialVersionUID = 2570177386504916045L;
    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;
    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}