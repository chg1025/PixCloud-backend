package com.chg.pixCloud.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chg.pixCloud.common.ErrorCode;
import com.chg.pixCloud.manager.FileManager;
import com.chg.pixCloud.mapper.PictureMapper;
import com.chg.pixCloud.model.dto.file.PictureUploadResult;
import com.chg.pixCloud.model.dto.picture.PictureQueryRequest;
import com.chg.pixCloud.model.dto.picture.PictureUploadRequest;
import com.chg.pixCloud.model.entity.Picture;
import com.chg.pixCloud.model.entity.User;
import com.chg.pixCloud.model.vo.PictureVO;
import com.chg.pixCloud.model.vo.UserVO;
import com.chg.pixCloud.service.PictureService;
import com.chg.pixCloud.service.UserService;
import com.chg.pixCloud.utils.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author c
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2024-12-30 16:09:20
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    FileManager fileManager;

    @Resource
    UserService userService;


    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest uploadRequest, User user) {
        // 校验参数
        ThrowUtils.throwIf(user == null, ErrorCode.NO_AUTH_ERROR);
        // 判断是新增还是修改
        Long picId = null;
        if (uploadRequest != null) {
            picId = uploadRequest.getId();
        }
        // 若是更新，判断图片是否存在
        if (picId != null) {
            boolean existed = this.lambdaQuery().eq(Picture::getId, picId).exists();
            ThrowUtils.throwIf(!existed, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        // 上传图片，得到图片信息
        // 根据用户划分目录
        String uploadPathPrefix = String.format("public/%s", user.getId());
        PictureUploadResult pictureUploadResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        // 填充入库信息
        Picture persistencePicture = persistencePictureInfo(user, pictureUploadResult);
        // 操作数据库
        boolean saved = this.saveOrUpdate(persistencePicture);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "数据库操作失败，图片上传失败");
        return PictureVO.objToVo(persistencePicture);
    }

    /**
     * 构造入库信息
     *
     * @param user                登录用户
     * @param pictureUploadResult 图片上传结果
     * @return 图片入库信息
     */
    private static Picture persistencePictureInfo(User user, PictureUploadResult pictureUploadResult) {
        Picture picture = new Picture();
        picture.setUrl(pictureUploadResult.getUrl());
        picture.setName(pictureUploadResult.getPicName());
        picture.setPicSize(pictureUploadResult.getPicSize());
        picture.setPicWidth(pictureUploadResult.getPicWidth());
        picture.setPicHeight(pictureUploadResult.getPicHeight());
        picture.setPicScale(pictureUploadResult.getPicScale());
        picture.setPicFormat(pictureUploadResult.getPicFormat());
        picture.setUserId(user.getId());
        return picture;
    }

    /**
     * 根据查询请求构造查询条件
     *
     * @param pictureQueryRequest 查询请求
     * @return 查询条件
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }


    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片详细信息
     * 查询优化：不是针对每条数据都查询一次用户，而是先获取到要查询的用户 id 列表，只发送一次查询用户表的请求，再将查到的值设置到图片对象中。
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 包括将图片对象列表转换为视图对象列表
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
        // 1. 关联查询用户信息
        // 1.1 提取用户id的Set集合，避免重复查询
        Set<Long> userIdSet = pictureList.stream()
                .map(Picture::getUserId)
                .collect(Collectors.toSet());
        // 1.2 查询用户信息，按用户 ID（User::getId）对用户信息分组，生成 Map<Long, List<User>>。
        // 其中 key 是用户 id，value 是对应的用户对象列表（通常一个 id 对应一个用户，List<User> 长度为 1）
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                // 获取用户分组中第一个用户对象
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 512, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 512, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }


}




