package com.chg.pixCloud.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chg.pixCloud.annotation.AuthCheck;
import com.chg.pixCloud.common.BaseResponse;
import com.chg.pixCloud.common.DeleteRequest;
import com.chg.pixCloud.common.ErrorCode;
import com.chg.pixCloud.constant.UserConstant;
import com.chg.pixCloud.model.dto.space.*;
import com.chg.pixCloud.model.entity.Space;
import com.chg.pixCloud.model.enums.SpaceLevelEnum;
import com.chg.pixCloud.model.vo.SpaceVO;
import com.chg.pixCloud.service.SpaceService;
import com.chg.pixCloud.service.UserService;
import com.chg.pixCloud.utils.ResultUtils;
import com.chg.pixCloud.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    SpaceService spaceService;
    @Resource
    UserService userService;

    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequest == null || request == null, ErrorCode.PARAMS_ERROR);
        long spaceId = spaceService.addSpace(spaceAddRequest, userService.getLoginUser(request));
        return ResultUtils.success(spaceId);
    }


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

    /**
     * 删除空间
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        spaceService.deleteSpace(deleteRequest, request);
        return ResultUtils.success(true);
    }

    /**
     * 编辑空间（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        spaceService.editSpace(spaceEditRequest, request);
        return ResultUtils.success(true);
    }


    /**
     * 根据 id 获取空间（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_USER)
    public BaseResponse<Space> getSpaceById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(space);
    }

    /**
     * 根据 id 获取图片（封装类）(普通用户使用)
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0 || request == null, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);

        SpaceVO spaceVO = SpaceVO.objToVo(space);
        return ResultUtils.success(spaceVO);
    }


    /**
     * 分页获取空间列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_USER)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        // 查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }

    /**
     * 分页获取空间列表（无缓存）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
                                                         HttpServletRequest request) {
        Page<SpaceVO> spaceVOByPage = spaceService.listSpaceVOByPage(spaceQueryRequest, request);
        return ResultUtils.success(spaceVOByPage);
    }


    /**
     * 获取所有空间级别信息
     *
     * @return 空间级别列表
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values()) // 获取所有枚举
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()))
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }


}


