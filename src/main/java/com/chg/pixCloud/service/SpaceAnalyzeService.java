package com.chg.pixCloud.service;

import com.chg.pixCloud.model.dto.space.analyze.*;
import com.chg.pixCloud.model.entity.Space;
import com.chg.pixCloud.model.entity.User;
import com.chg.pixCloud.model.vo.space.analyze.*;

import java.util.List;

public interface SpaceAnalyzeService {

    /**
     * 空间使用分析
     *
     * @param spaceUsageAnalyzeRequest 空间使用分析请求
     * @param loginUser                登录用户
     * @return 空间使用分析响应
     */
    SpaceUsageAnalyzeResponse spaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 空间图片分类分析
     *
     * @param spaceAnalyzeRequest 空间分析请求对象，包含查询条件和空间ID等信息
     * @param loginUser           当前登录的用户对象
     * @return 空间图片分类分析结果列表
     */
    List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyze(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser);

    /**
     * 空间图片标签分析
     *
     * @param spaceTagAnalyzeRequest 空间图片标签分析请求对象，包含查询条件和空间ID等信息
     * @param loginUser              当前登录的用户对象
     * @return 空间图片标签分析结果列表
     */
    List<SpaceTagAnalyzeResponse> spaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 空间图片大小分析
     *
     * @param spaceSizeAnalyzeRequest 空间图片大小分析请求对象，包含查询条件和空间ID等信息
     *                                图片大小范围的单位为字节
     * @param loginUser               当前登录的用户对象
     * @return 空间图片大小分析结果列表
     */
    List<SpaceSizeAnalyzeResponse> spaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    /**
     * 用户上传行为分析
     *
     * @param spaceUserAnalyzeRequest 用户上传行为分析请求对象，包含查询条件和空间ID等信息
     * @param loginUser               当前登录的用户对象
     * @return 空间图片大小分析结果列表
     */
    List<SpaceUserAnalyzeResponse> spaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    /**
     * 空间使用排行分析（仅管理员）
     *
     * @param spaceRankAnalyzeRequest 空间排行分析请求对象
     * @param loginUser               当前登录的用户对象
     * @return 空间排行列表
     */
    List<Space> spaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
