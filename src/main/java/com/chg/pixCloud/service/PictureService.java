package com.chg.pixCloud.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chg.pixCloud.model.dto.picture.PictureQueryRequest;
import com.chg.pixCloud.model.dto.picture.PictureReviewRequest;
import com.chg.pixCloud.model.dto.picture.PictureUploadByBatchRequest;
import com.chg.pixCloud.model.dto.picture.PictureUploadRequest;
import com.chg.pixCloud.model.entity.Picture;
import com.chg.pixCloud.model.entity.User;
import com.chg.pixCloud.model.vo.PictureVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author chg
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2024-12-30 16:09:20
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inputSource   图片输入源
     * @param uploadRequest 图片id
     * @param user          上传信息
     * @return 图片信息
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest uploadRequest, User user);

    /**
     * 根据查询请求构造查询条件
     *
     * @param pictureQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 根据图片信息获取图片详情
     *
     * @param picture 原图片信息
     * @param request 查询请求
     * @return 封装后的图片信息
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页获取图片详细信息
     * 查询优化：不是针对每条数据都查询一次用户，而是先获取到要查询的用户 id 列表，只发送一次查询用户表的请求，再将查到的值设置到图片对象中。
     *
     * @param picturePage 图片分页
     * @param request     分页查询请求
     * @return 分页的图片详细信息
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 图片数据校验，用于更新和修改图片时进行判断
     *
     * @param picture 图片
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser            登录用户
     */
    void pictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核字段
     *
     * @param picture   图片信息
     * @param loginUser 登录用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和上传图片
     *
     * @param pictureUploadByBatchRequest 抓取请求
     * @param loginUser                   登录用户
     * @return 上传成功的图片数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);
}
