package com.chg.pixCloud.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chg.pixCloud.common.DeleteRequest;
import com.chg.pixCloud.model.dto.space.SpaceAddRequest;
import com.chg.pixCloud.model.dto.space.SpaceEditRequest;
import com.chg.pixCloud.model.dto.space.SpaceQueryRequest;
import com.chg.pixCloud.model.dto.space.SpaceUpdateRequest;
import com.chg.pixCloud.model.entity.Space;
import com.chg.pixCloud.model.entity.User;
import com.chg.pixCloud.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author c
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-01-02 22:22:17
 */
public interface SpaceService extends IService<Space> {

    /**
     * 根据空间信息获取空间详情
     *
     * @param space   原空间信息
     * @param request 查询请求
     * @return 封装后的空间信息
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 分页获取空间详细信息
     * 查询优化：不是针对每条数据都查询一次用户，而是先获取到要查询的用户 id 列表，只发送一次查询用户表的请求，再将查到的值设置到空间对象中。
     *
     * @param spacePage 空间分页
     * @param request   分页查询请求
     * @return 分页的空间详细信息
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 根据查询请求构造查询条件
     *
     * @param spaceQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);


    /**
     * 更新空间（仅管理员）
     *
     * @param spaceUpdateRequest 空间更新请求
     */
    void updateSpace(SpaceUpdateRequest spaceUpdateRequest);

    /**
     * 创建空间
     *
     * @param spaceAddRequest 创建空间请求
     * @param loginUser       登录用户
     * @return 创建成功数量
     * @description 使用本地 synchronized 锁对 userId 进行加锁，
     * 这样不同的用户可以拿到不同的锁，对性能的影响较低。
     * 在加锁的代码中，我们使用 Spring 的 编程式事务管理器 transactionTemplate 封装跟数据库有关的查询和插入操作，
     * 而不是使用 @Transactional 注解来控制事务，这样可以保证事务的提交在加锁的范围内。
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 删除空间
     *
     * @param deleteRequest
     * @param request
     */
    void deleteSpace(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 编辑空间
     *
     * @param spaceEditRequest
     */
    void editSpace(SpaceEditRequest spaceEditRequest, HttpServletRequest request);

    Page<SpaceVO> listSpaceVOByPage(SpaceQueryRequest spaceQueryRequest, HttpServletRequest request);

    /**
     * 校验空间数据
     *
     * @param space 空间信息
     * @param add   是否是创建
     */
    void validSpace(Space space, boolean add);

    /**
     * 根据空间级别，自动填充限额数据
     *
     * @param space 空间
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 空间权限校验
     *
     * @param loginUser 登录用户
     * @param space     待校验的空间
     */
    void checkSpaceAuth(User loginUser, Space space);
}
