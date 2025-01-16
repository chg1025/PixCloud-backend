package com.chg.pixCloud.controller;

import com.chg.pixCloud.common.BaseResponse;
import com.chg.pixCloud.common.ErrorCode;
import com.chg.pixCloud.model.dto.space.analyze.*;
import com.chg.pixCloud.model.entity.Space;
import com.chg.pixCloud.model.entity.User;
import com.chg.pixCloud.model.vo.space.analyze.*;
import com.chg.pixCloud.service.SpaceAnalyzeService;
import com.chg.pixCloud.service.UserService;
import com.chg.pixCloud.utils.ResultUtils;
import com.chg.pixCloud.utils.ThrowUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {

    @Resource
    private UserService userService;
    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    /**
     * 空间使用分析
     *
     * @param spaceUsageAnalyzeRequest 请求对象，包含分析所需的参数
     * @param request                  HTTP请求对象，用于获取登录用户信息
     * @return 返回空间使用分析结果
     */
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(
            @RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null || request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = spaceAnalyzeService.spaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUsageAnalyzeResponse);
    }

    /**
     * 空间图片分类分析
     *
     * @param spaceCategoryAnalyzeRequest 请求对象，包含分析所需的参数
     * @param request                     HTTP请求对象，用于获取登录用户信息
     * @return 返回空间图片分类分析结果
     */
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(
            @RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null || request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyzeResponseList = spaceAnalyzeService.spaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceCategoryAnalyzeResponseList);
    }

    /**
     * 空间图片标签分析
     *
     * @param spaceTagAnalyzeRequest 请求对象，包含分析所需的参数
     * @param request                HTTP请求对象，用于获取登录用户信息
     * @return 返回空间图片标签分析结果
     */
    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(
            @RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null || request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceTagAnalyzeResponse> spaceTagAnalyzeResponseList = spaceAnalyzeService.spaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceTagAnalyzeResponseList);
    }

    /**
     * 空间图片大小分析
     *
     * @param spaceSizeAnalyzeRequest 请求对象，包含分析所需的参数
     * @param request                 HTTP请求对象，用于获取登录用户信息
     * @return 返回空间图片标签分析结果
     */
    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null || request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceSizeAnalyzeResponse> spaceSizeAnalyzeResponseList = spaceAnalyzeService.spaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceSizeAnalyzeResponseList);
    }

    /**
     * 用户上传行为分析
     *
     * @param spaceUserAnalyzeRequest 请求对象，包含分析所需的参数
     * @param request                 HTTP请求对象，用于获取登录用户信息
     * @return 返回用户上传行为分析结果
     */
    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null || request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceUserAnalyzeResponse> spaceUserAnalyzeResponseList = spaceAnalyzeService.spaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUserAnalyzeResponseList);
    }

    /**
     * 空间使用排行分析（仅管理员）
     *
     * @param spaceRankAnalyzeRequest 请求对象，包含分析所需的参数
     * @param request                 HTTP请求对象，用于获取登录用户信息
     * @return 返回空间排行分析结果
     */
    @PostMapping("/rank")
    public BaseResponse<List<Space>> getSpaceRankAnalyze(@RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null || request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<Space> spaceList = spaceAnalyzeService.spaceRankAnalyze(spaceRankAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceList);
    }
}














