package com.chg.pixCloud.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.chg.pixCloud.common.ErrorCode;
import com.chg.pixCloud.config.CosClientConfig;
import com.chg.pixCloud.exception.BusinessException;
import com.chg.pixCloud.manager.CosManager;
import com.chg.pixCloud.model.dto.file.PictureUploadResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;

/**
 * 图片上传模版(抽象类)
 */
@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    CosClientConfig cosClientConfig;
    @Resource
    COSClient cosClient;
    @Resource
    CosManager cosManager;

    /**
     * 校验输入源（本地文件或URL）
     *
     * @param inputSource 文件输入源
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取输入源的原始文件名
     *
     * @param inputSource 文件输入源
     * @return 文件名
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 处理文件输入源生成本地临时文件
     *
     * @param inputSource 文件输入源
     * @param file        本地临时文件地址
     */
    protected abstract void processFIle(Object inputSource, File file);

    /**
     * 上传图片
     *
     * @param inputSource      文件输入源
     * @param uploadPathPrefix 上传文件名前缀
     * @return 图片上传结果
     */
    public PictureUploadResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. 校验图片
        validPicture(inputSource);
        // 2. 获取图片上传地址
        String uuid = RandomUtil.randomString(16);
        // 获取文件名称
        String originalFilename = getOriginalFilename(inputSource);
        // 自定义上传文件的名称，增加安全性
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        File file = null;
        try {
            // 3. 创建临时文件，获取文件到服务器
            file = File.createTempFile(uploadPath, null);
            // 处理输入来源
            processFIle(inputSource, file);
            // 4. 上传文件到对象存储
            PutObjectResult putObjectResult = cosManager.putAndOptionPictureToCOS(uploadPath, file);
            // 5. 获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 6. 返回文件上传结果
            return getPictureUploadResult(imageInfo, uploadPath, originalFilename, file);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 7. 临时文件清理
            if (file != null) {
                deleteTempFile(file);
            }
        }
    }

    /**
     * 封装返回结果
     *
     * @param imageInfo        对象存储返回的图片信息
     * @param uploadPath       上传文件路径
     * @param originalFilename 原始文件名称
     * @param file             临时文件
     * @return 上传文件返回对象
     */
    private PictureUploadResult getPictureUploadResult(ImageInfo imageInfo, String uploadPath, String originalFilename, File file) {
        // 封装返回结果
        int imageWidth = imageInfo.getWidth();
        int imageHeight = imageInfo.getHeight();
        double imageScale = NumberUtil.round(imageWidth * 1.0 / imageHeight, 2).doubleValue();

        PictureUploadResult pictureUploadResult = new PictureUploadResult();
        pictureUploadResult.setUrl(cosClientConfig.getHost() + uploadPath);
        pictureUploadResult.setPicName(FileUtil.mainName(originalFilename));
        pictureUploadResult.setPicSize(FileUtil.size(file));
        pictureUploadResult.setPicWidth(imageWidth);
        pictureUploadResult.setPicHeight(imageHeight);
        pictureUploadResult.setPicScale(imageScale);
        pictureUploadResult.setPicFormat(imageInfo.getFormat());
        return pictureUploadResult;
    }

    /**
     * 删除临时文件
     *
     * @param file 临时文件
     */
    private static void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean deleted = file.delete();
        if (!deleted) {
            log.error("file delete error, filePath={}", file.getAbsolutePath());
        }
    }
}





