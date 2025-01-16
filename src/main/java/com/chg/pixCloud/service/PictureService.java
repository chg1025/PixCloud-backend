package com.chg.pixCloud.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chg.pixCloud.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.chg.pixCloud.common.DeleteRequest;
import com.chg.pixCloud.model.dto.picture.*;
import com.chg.pixCloud.model.entity.Picture;
import com.chg.pixCloud.model.entity.User;
import com.chg.pixCloud.model.vo.PictureVO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author chg
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2024-12-30 16:09:20
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片（可重新上传）
     *
     * @param inputSource   图片输入源
     * @param uploadRequest 图片id
     * @param user          上传信息
     * @return 上传后的图片信息
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
    void fillReviewParams(Picture picture, User loginUser, long spaceId);

    /**
     * 批量抓取和上传图片
     *
     * @param pictureUploadByBatchRequest 抓取请求
     * @param loginUser                   登录用户
     * @return 上传成功的图片数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    /**
     * 删除图片
     *
     * @param deleteRequest 删除id
     * @param request       删除请求
     */
    void deletePicture(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 编辑图片
     *
     * @param pictureEditRequest 图片修改参数
     * @param request            编辑请求
     */
    void editPicture(PictureEditRequest pictureEditRequest, HttpServletRequest request);

    /**
     * 分页获取图片列表（普通用户）
     *
     * @param pictureQueryRequest 分页查询请求
     * @param request             登录态
     * @return 图片列表
     */
    Page<PictureVO> listPictureVOByPage(PictureQueryRequest pictureQueryRequest, HttpServletRequest request);

    /**
     * 分页获取图片列表（有缓存）
     *
     * @param pictureQueryRequest 分页查询请求
     * @param request             登录态
     * @return 图片列表
     */
    Page<PictureVO> listPictureVOByPageWithCache(PictureQueryRequest pictureQueryRequest,
                                                 HttpServletRequest request);

    /**
     * 清理图片
     *
     * @param oldPicture 清理图片
     */
    @Async
    void clearPictureFile(Picture oldPicture);

    /**
     * 鉴权（校验当前登录用户对某图片是否有编辑/删除权限）
     *
     * @param loginUser 当前登录用户
     * @param picture   图片
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 根据id查询图片（用户）
     *
     * @param id      图片id
     * @param request 查询请求
     * @return 图片封装信息
     */
    PictureVO getPictureVOById(long id, HttpServletRequest request);

    /**
     * 根据颜色搜索图片
     *
     * @param spaceId   空间ID
     * @param picColor  图片颜色
     * @param loginUser 登录用户
     * @return 搜索结果列表
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 批量编辑图片信息
     *
     * @param pictureEditByBatchRequest 批量编辑图片请求
     * @param loginUser                 登录用户
     */
    @Transactional(rollbackFor = Exception.class)
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    /**
     * 创建扩图任务
     *
     * @param createPictureOutPaintingTaskRequest 创建扩图任务请求
     * @param loginUser                           登录用户
     * @return 创建扩图任务响应
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);

    /**
     * 刷新所有与 listPictureVOByPage 相关的缓存
     *
     * @return 刷新状态
     */
    boolean refreshAllListPictureVOByPageCache();
}
