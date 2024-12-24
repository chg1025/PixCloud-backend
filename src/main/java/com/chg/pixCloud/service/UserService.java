package com.chg.pixCloud.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chg.pixCloud.model.entity.User;
import com.chg.pixCloud.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author c
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2024-12-24 21:15:56
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param password      密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String password, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount 用户账号
     * @param password    密码
     * @return 脱敏用户信息
     */
    LoginUserVO userLogin(String userAccount, String password, HttpServletRequest request);


    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取登录后的用户脱敏信息
     *
     * @param user 用户
     * @return 脱敏用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户注销
     *
     * @param request 请求
     * @return true
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取加密后的密码
     *
     * @param password
     * @return
     */
    String getEncryptPassword(String password);
}
