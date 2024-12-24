package com.chg.pixCloud.model.dto;

import lombok.Data;

import java.io.Serializable;


/**
 * The type User login request.
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 536187066103172426L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String password;


}
