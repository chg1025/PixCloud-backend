package com.chg.pixCloud.controller;

import com.chg.pixCloud.annotation.AuthCheck;
import com.chg.pixCloud.common.BaseResponse;
import com.chg.pixCloud.constant.UserConstant;
import com.chg.pixCloud.model.dto.space.SpaceUpdateRequest;
import com.chg.pixCloud.service.SpaceService;
import com.chg.pixCloud.service.UserService;
import com.chg.pixCloud.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    UserService userService;

    @Resource
    SpaceService spaceService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    /**
     * 更新空间信息（仅管理员）
     *
     * @param spaceUpdateRequest 空间更新请求
     * @return 更新结果
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_USER)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        spaceService.updateSpace(spaceUpdateRequest);
        return ResultUtils.success(true);
    }



}


