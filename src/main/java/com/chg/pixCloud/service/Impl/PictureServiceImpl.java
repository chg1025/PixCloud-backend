package com.chg.pixCloud.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chg.pixCloud.common.ErrorCode;
import com.chg.pixCloud.exception.BusinessException;
import com.chg.pixCloud.manager.CosManager;
import com.chg.pixCloud.manager.upload.FilePictureUpload;
import com.chg.pixCloud.manager.upload.PictureUploadTemplate;
import com.chg.pixCloud.manager.upload.UrlPictureUpload;
import com.chg.pixCloud.mapper.PictureMapper;
import com.chg.pixCloud.model.dto.file.PictureUploadResult;
import com.chg.pixCloud.model.dto.picture.PictureQueryRequest;
import com.chg.pixCloud.model.dto.picture.PictureReviewRequest;
import com.chg.pixCloud.model.dto.picture.PictureUploadByBatchRequest;
import com.chg.pixCloud.model.dto.picture.PictureUploadRequest;
import com.chg.pixCloud.model.entity.Picture;
import com.chg.pixCloud.model.entity.User;
import com.chg.pixCloud.model.enums.PictureReviewStatusEnum;
import com.chg.pixCloud.model.vo.PictureVO;
import com.chg.pixCloud.model.vo.UserVO;
import com.chg.pixCloud.service.PictureService;
import com.chg.pixCloud.service.UserService;
import com.chg.pixCloud.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author c
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2024-12-30 16:09:20
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    FilePictureUpload filePictureUpload;
    @Resource
    UrlPictureUpload urlPictureUpload;
    @Resource
    UserService userService;
    @Resource
    CosManager cosManager;


    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest uploadRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 判断是新增还是修改
        Long picId = null;
        if (uploadRequest != null) {
            picId = uploadRequest.getId();
            log.info("图片id: {}", picId);
        }
        // 若是更新，判断图片是否存在
        if (picId != null && picId > 0) {
            Picture oldPicture = this.getById(picId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人或管理员可编辑图片
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 对象存储中的旧图片删除
            this.clearPictureFile(oldPicture);
        }
        // 上传图片到对象存储，得到图片信息
        // 根据用户划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        // 根据inputSource上传类型，区分上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        PictureUploadResult pictureUploadResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 填充入库信息
        Picture persistencePicture = persistencePictureInfo(loginUser, pictureUploadResult, uploadRequest);
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
    private Picture persistencePictureInfo(User user, PictureUploadResult pictureUploadResult, PictureUploadRequest uploadRequest) {
        Picture picture = new Picture();
        picture.setUrl(pictureUploadResult.getUrl());
        picture.setThumbnailUrl(pictureUploadResult.getThumbnailUrl());
        // 支持外层传递图片名称
        String picName = pictureUploadResult.getPicName();
        if (uploadRequest != null && StrUtil.isNotBlank(uploadRequest.getName())) {
            picName = uploadRequest.getName();
        }
        picture.setName(picName);
        picture.setPicSize(pictureUploadResult.getPicSize());
        picture.setPicWidth(pictureUploadResult.getPicWidth());
        picture.setPicHeight(pictureUploadResult.getPicHeight());
        picture.setPicScale(pictureUploadResult.getPicScale());
        picture.setPicFormat(pictureUploadResult.getPicFormat());
        picture.setUserId(user.getId());
        this.fillReviewParams(picture, user);
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
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
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
        queryWrapper.like(ObjUtil.isNotEmpty(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
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

    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser            登录用户
     */
    @Override
    public void pictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum pictureReviewStatusEnum = PictureReviewStatusEnum.getPictureReviewStatusEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        if (id == null || pictureReviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(pictureReviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 判断图片是否存在
        Picture picture = this.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 3. 校验审核状态是否重复
        ThrowUtils.throwIf(picture.getReviewStatus().equals(reviewStatus), ErrorCode.PARAMS_ERROR, "请勿重复审核");
        // 4. 数据库操作
        Picture updatePicture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean updated = this.updateById(updatePicture);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 填充审核字段
     *
     * @param picture   图片信息
     * @param loginUser 登录用户
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("管理员自动过审");
        } else {
            // 非管理员，编辑后置为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    /**
     * 批量抓取图片
     *
     * @param pictureUploadByBatchRequest 抓取请求
     * @param loginUser                   登录用户
     * @return 上传成功的图片数量
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        int count = 0;
        // 校验参数
        String q = pictureUploadByBatchRequest.getQ();
        int current = pictureUploadByBatchRequest.getCurrent();
        int pageSize = pictureUploadByBatchRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 30, ErrorCode.PARAMS_ERROR);
        // 抓取内容
        List<String> imageUrls = new ArrayList<>();
        // 搜索词应进行URL编码
        String url = String.format("https://cn.bing.com/images/async?q=%s&first=%s&count=%s&mmasync=1", URLUtil.encode(q), current, pageSize);
        try {
            // 使用 Jsoup 连接并获取文档
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Apple Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36")
                    .timeout(20000)
                    .get();
            // 解析内容
            // 获取所有图片的元素（<img> 标签）
            Elements imgElements = document.select("img");

            // 提取每个图片的 URL
            imageUrls = new ArrayList<>();
            for (Element imgElement : imgElements) {
                String imgUrl = imgElement.attr("src"); // 获取图片 src 属性
                if (StrUtil.isNotBlank(imgUrl)) {
                    imageUrls.add(URLUtil.getPath(imgUrl));
                }
                try {
                    // 上传图片
                    // TODO 待优化成批量上传至对象存储及数据库
                    PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                    pictureUploadRequest.setUrl(imgUrl);
                    // 图片名称前缀默认为搜索词
                    String namePrefix = pictureUploadByBatchRequest.getNamePrefix() != null ? pictureUploadByBatchRequest.getNamePrefix() : q;
                    pictureUploadRequest.setName(namePrefix + (count + 1));
                    PictureVO pictureVO = this.uploadPicture(imgUrl, pictureUploadRequest, loginUser);
                    log.info("图片上传成功id:[{}]", pictureVO.getId());
                    count++;
                } catch (Exception e) {
                    log.error("图片上传失败Url:[{}]", imgUrl);
                    continue;
                }
                if (count >= pageSize) {
                    break;
                }
            }
        } catch (IOException e) {
            log.error("{}获取内容失败：{}", url, e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取内容出错啦，请稍后重试..");
        }
        log.info("上传图片数量: [{}]", count);
        return count;
    }

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        // 清理压缩图
        cosManager.deleteObject(oldPicture.getUrl());
        // 清理缩略图
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }

}




