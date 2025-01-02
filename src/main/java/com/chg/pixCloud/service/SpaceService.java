package com.chg.pixCloud.service;

import com.chg.pixCloud.model.dto.space.SpaceAddRequest;
import com.chg.pixCloud.model.dto.space.SpaceUpdateRequest;
import com.chg.pixCloud.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chg.pixCloud.model.entity.User;

/**
 * @author c
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-01-02 22:22:17
 */
public interface SpaceService extends IService<Space> {

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
}
