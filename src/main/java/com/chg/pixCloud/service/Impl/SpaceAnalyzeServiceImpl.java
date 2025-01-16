package com.chg.pixCloud.service.Impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chg.pixCloud.common.ErrorCode;
import com.chg.pixCloud.exception.BusinessException;
import com.chg.pixCloud.model.dto.space.analyze.*;
import com.chg.pixCloud.model.entity.Picture;
import com.chg.pixCloud.model.entity.Space;
import com.chg.pixCloud.model.entity.User;
import com.chg.pixCloud.model.vo.space.analyze.*;
import com.chg.pixCloud.service.PictureService;
import com.chg.pixCloud.service.SpaceAnalyzeService;
import com.chg.pixCloud.service.SpaceService;
import com.chg.pixCloud.service.UserService;
import com.chg.pixCloud.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.chg.pixCloud.common.Constants.*;

@Slf4j
@Service
public class SpaceAnalyzeServiceImpl implements SpaceAnalyzeService {
    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private PictureService pictureService;


    /**
     * 空间使用分析
     *
     * @param spaceUsageAnalyzeRequest 空间使用分析请求
     * @param loginUser                登录用户
     * @return 空间使用分析响应
     */
    @Override
    public SpaceUsageAnalyzeResponse spaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR);
        if (spaceUsageAnalyzeRequest.isQueryAll() || spaceUsageAnalyzeRequest.isQueryPublic()) {
            // 全空间 或者 公共图库 分析, 权限校验：仅管理员可访问
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问公共图库");
            // 统计空间使用情况
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            // 仅查询图片大小，提高查询效率并节约内存
            queryWrapper.select("picSize");
            if (!spaceUsageAnalyzeRequest.isQueryAll()) {
                // 查询公共图库
                queryWrapper.isNull("spaceId");
            }
            List<Object> pictureObjList = pictureService.getBaseMapper().selectObjs(queryWrapper);
            long useSize = pictureObjList.stream().mapToLong(obj -> obj instanceof Long ? (Long) obj : 0L).sum();
            long useCount = pictureObjList.size();
            // 返回结果
            return new SpaceUsageAnalyzeResponse(useSize, useCount);
        } else {
            // 私有空间分析
            Long spaceId;
            try {
                spaceId = spaceUsageAnalyzeRequest.getSpaceId();
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            // 获取空间信息
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 权限校验
            spaceService.checkSpaceAuth(loginUser, space);
            // 统计空间使用情况
            double sizeUsageRatio = space.getTotalSize() * 100.0 / space.getMaxSize().doubleValue();
            double countUsageRatio = space.getTotalCount() * 100.0 / space.getMaxCount().doubleValue();
            // 返回结果
            return new SpaceUsageAnalyzeResponse(space.getTotalSize(), space.getTotalCount(), space.getMaxSize(), space.getMaxCount(), sizeUsageRatio, countUsageRatio);
        }
    }


    /**
     * 空间图片分类分析
     *
     * @param spaceAnalyzeRequest 空间分析请求对象，包含查询条件和空间ID等信息
     * @param loginUser           当前登录的用户对象
     * @return 空间图片分类分析结果列表
     */
    @Override
    public List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyze(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 校验权限
        checkSpaceAnalyzeAuth(spaceAnalyzeRequest, loginUser);
        // 构建查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceAnalyzeRequest, queryWrapper);
        // 使用 Mybatis-Plus 分组查询
        queryWrapper.select("category as category", "count(*) as count", "sum(picSize) as totalSize").groupBy("category");
        // 执行查询并转换结果
        return pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(obj -> {
            String category = obj.get("category") != null ? obj.get("category").toString() : "未分类";
            Long count = ((Number) obj.get("count")).longValue();
            Long totalSize = ((Number) obj.get("totalSize")).longValue();
            return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
        }).collect(Collectors.toList());

    }

    /**
     * 空间图片标签分析
     *
     * @param spaceTagAnalyzeRequest 空间图片标签分析请求对象，包含查询条件和空间ID等信息
     * @param loginUser              当前登录的用户对象
     * @return 空间图片标签分析结果列表
     */
    @Override
    public List<SpaceTagAnalyzeResponse> spaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 校验权限
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);
        // 构建查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);
        queryWrapper.select("tags");
        List<String> tagsJsonList = pictureService.getBaseMapper().selectObjs(queryWrapper).stream().filter(ObjUtil::isNotEmpty).map(Object::toString).toList();
        // 合并所有标签，并统计每个标签的出现次数
        Map<String, Long> tagsCount = tagsJsonList.stream().flatMap(tagsJson -> JSONUtil.toList(tagsJson, String.class).stream()).collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));
        // 将结果转换为 SpaceTagAnalyzeResponse 对象列表
        return tagsCount.entrySet().stream().map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    /**
     * 空间图片大小分析
     *
     * @param spaceSizeAnalyzeRequest 空间图片大小分析请求对象，包含查询条件和空间ID等信息
     *                                图片大小范围的单位为字节
     * @param loginUser               当前登录的用户对象
     * @return 空间图片大小分析结果列表
     */
    @Override
    public List<SpaceSizeAnalyzeResponse> spaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);
        queryWrapper.select("picSize");
        List<Long> pictureSizes = pictureService.getBaseMapper().selectObjs(queryWrapper).stream().map(obj -> obj instanceof Long ? (Long) obj : 0L).toList();
        LinkedHashMap<String, Long> sizeRanges = new LinkedHashMap<>();
        sizeRanges.put("<100KB", pictureSizes.stream().filter(size -> size < SIZE_100KB).count());
        sizeRanges.put("100KB-1MB", pictureSizes.stream().filter(size -> size >= SIZE_100KB && size < SIZE_1MB).count());
        sizeRanges.put("1MB-2MB", pictureSizes.stream().filter(size -> size >= SIZE_1MB && size < SIZE_2MB).count());
        sizeRanges.put(">2MB", pictureSizes.stream().filter(size -> size >= SIZE_2MB).count());
        return sizeRanges.entrySet().stream().map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue())).toList();
    }

    /**
     * 用户上传行为分析
     *
     * @param spaceUserAnalyzeRequest 用户上传行为分析请求对象，包含查询条件和空间ID等信息
     * @param loginUser               当前登录的用户对象
     * @return 空间图片大小分析结果列表
     */
    @Override
    public List<SpaceUserAnalyzeResponse> spaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 权限校验
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);
        // 构建查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);
        Long userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);

        // 分析维度：每日、每周、每月
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m-%d') as period", "count(*) as count");
                break;
            case "week":
                queryWrapper.select("YEARWEEK(createTime) as period", " count(*) as count");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m') as period", "count(*) as count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "时间维度错误");
        }
        // 分组查询
        queryWrapper.groupBy("period").orderByAsc("period");
        // 执行查询并转换结果
        return pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(obj -> {
            String period = obj.get("period") != null ? obj.get("period").toString() : "";
            Long count = ((Number) obj.get("count")).longValue();
            return new SpaceUserAnalyzeResponse(period, count);
        }).toList();

    }

    /**
     * 空间使用排行分析（仅管理员）
     *
     * @param spaceRankAnalyzeRequest 空间排行分析请求对象
     * @param loginUser               当前登录的用户对象
     * @return 空间排行列表
     */
    @Override
    public List<Space> spaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 仅管理员可查看空间排行
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        // 构造查询条件
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "spaceName", "userId", "totalSize").orderByDesc("totalSize").last("LIMIT " + spaceRankAnalyzeRequest.getTopN()); // 取前 N 名
        // 查询结果
        return spaceService.list(queryWrapper);
    }


    /**
     * 校验空间分析权限
     *
     * @param spaceAnalyzeRequest 空间分析请求对象，包含查询条件和空间ID等信息
     * @param loginUser           当前登录的用户对象
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        // 检查权限
        if (spaceAnalyzeRequest.isQueryAll() || spaceAnalyzeRequest.isQueryPublic()) {
            // 全空间分析或者公共图库权限校验：仅管理员可访问
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问公共图库");
        } else {
            // 私有空间权限校验
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            spaceService.checkSpaceAuth(loginUser, space);
        }
    }

    /**
     * 填充分析查询参数
     *
     * @param spaceAnalyzeRequest 空间分析请求对象，包含查询条件等信息
     * @param queryWrapper        MyBatis-Plus的查询包装器，用于构建查询条件
     */
    private static void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        if (spaceAnalyzeRequest.isQueryAll()) {
            return;
        }
        if (spaceAnalyzeRequest.isQueryPublic()) {
            queryWrapper.isNull("spaceId");
            return;
        }
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }


}
