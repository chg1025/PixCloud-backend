package com.chg.pixCloud.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chg.pixCloud.common.ErrorCode;
import com.chg.pixCloud.constant.UserConstant;
import com.chg.pixCloud.exception.BusinessException;
import com.chg.pixCloud.model.dto.user.UserQueryRequest;
import com.chg.pixCloud.model.entity.User;
import com.chg.pixCloud.model.enums.UserRoleEnum;
import com.chg.pixCloud.model.vo.LoginUserVO;
import com.chg.pixCloud.model.vo.UserVO;
import com.chg.pixCloud.service.UserService;
import com.chg.pixCloud.mapper.UserMapper;
import com.chg.pixCloud.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author c
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-12-24 21:15:56
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param password      密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public long userRegister(String userAccount, String password, String checkPassword) {
        // 1. 校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, password, checkPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(password.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        ThrowUtils.throwIf(!password.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
        // 2. 检查用户账号是否和数据库中已有账号重复
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号重复");
        // 3. 密码加密
        String encryptPassword = getEncryptPassword(password);
        // 4. 插入数据到数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("Dreamer");
        user.setUserRole(UserRoleEnum.USER.getValue());

        try {
            boolean saveResult = this.save(user);
            ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "系统错误，数据库异常");
        } catch (DuplicateKeyException e) {
            log.error("用户注册失败，账号已存在: {}", userAccount, e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误，请稍后重试");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount 用户账号
     * @param password    密码
     * @return 脱敏用户信息
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String password, HttpServletRequest request) {
        // 1. 参数校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, password), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(password.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");

        // 2. 对用户密码加密
        String encryptPassword = getEncryptPassword(password);

        // 3. 查询数据库用户，验证是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        queryWrapper.eq(User::getUserPassword, encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount: {}, userAccount cannot match userPassword ", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 4. 保存用户登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 判断是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        ThrowUtils.throwIf(currentUser == null || currentUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);

        // 从数据库中查询（若追求性能，可以直接注释，返回上述结果）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        return currentUser;
    }

    /**
     * 获取登录后的用户脱敏信息
     *
     * @param user 用户
     * @return 脱敏用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 用户注销
     *
     * @param request 请求
     * @return true
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 判断是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(userObj == null, ErrorCode.OPERATION_ERROR, "未登录");
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }


    /**
     * 获取加密后的密码
     *
     * @param password 密码
     * @return 加密密码
     */
    @Override
    public String getEncryptPassword(String password) {
        final String SALT = "PixCloudByChg@~*^";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }
}




