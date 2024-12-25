package com.chg.pixCloud.model.dto.user;

import lombok.Data;

import java.io.Serializable;


/**
 * The type User register request.
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -2271860216614206255L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String password;

    /**
     * 确认密码
     */
    private String checkPassword;

}
