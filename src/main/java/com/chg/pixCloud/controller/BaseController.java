package com.chg.pixCloud.controller;

import com.chg.pixCloud.model.entity.User;
import com.chg.pixCloud.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Component
public class BaseController {

    @Resource
    private UserService userService;

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 用户信息
     */
    public User getCurrentUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            return null;
        }
        return loginUser;
    }
}
