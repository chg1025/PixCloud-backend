package com.chg.pixCloud.aop;

import com.chg.pixCloud.annotation.AuthCheck;
import com.chg.pixCloud.common.ErrorCode;
import com.chg.pixCloud.model.entity.User;
import com.chg.pixCloud.model.enums.UserRoleEnum;
import com.chg.pixCloud.service.UserService;
import com.chg.pixCloud.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Slf4j
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param pjp       切入点
     * @param authCheck 权限校验注解
     */
    @Around("@annotation(authCheck)")
    public Object doIntercept(ProceedingJoinPoint pjp, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        log.info("用户[{}]权限校验", loginUser.getUserName());
        UserRoleEnum mustRoleEnum = UserRoleEnum.getUserRoleEnumByValue(mustRole);
        // 不需要权限，直接放行
        if (mustRoleEnum == null) {
            return pjp.proceed();
        }
        // 以下必须有权限才能通过
        // 1. 获取当前用户具有的权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getUserRoleEnumByValue(loginUser.getUserRole());
        // 用户无权限，拒绝
        ThrowUtils.throwIf(userRoleEnum == null, ErrorCode.NO_AUTH_ERROR);
        // 要求为管理员权限，用户无管理员权限，拒绝
        ThrowUtils.throwIf(UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum), ErrorCode.NO_AUTH_ERROR);
        // 通过权限校验，放行
        return pjp.proceed();
    }
}
