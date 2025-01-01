package com.chg.pixCloud.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.chg.pixCloud.common.ErrorCode;
import com.chg.pixCloud.config.CosClientConfig;
import com.chg.pixCloud.exception.BusinessException;
import com.chg.pixCloud.model.dto.file.PictureUploadResult;
import com.chg.pixCloud.utils.ThrowUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 抽象为模版方法实现，此服务已废弃
 */
@Slf4j
@Service
@Deprecated
public class FileManager {
    @Resource
    CosClientConfig cosClientConfig;
    @Resource
    COSClient cosClient;
    @Resource
    CosManager cosManager;

    /**
     * 上传图片
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 上传文件名前缀
     * @return 图片上传结果
     */
    public PictureUploadResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验图片
        validPicture(multipartFile);
        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        // 自定义上传文件的名称，增加安全性
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        // 上传路径
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);

        // 3. 上传文件
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putAndOptionPictureToCOS(uploadPath, file);
            // 获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
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

            // 4. 返回可访问文件的地址
            return pictureUploadResult;
        } catch (Exception e) {
            log.error("图片上传到对象存储失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 临时文件清理
            if (file != null) {
                deleteTempFile(file);
            }
        }


    }


    /**
     * 校验文件
     *
     * @param file
     */
    private void validPicture(MultipartFile file) {
        // 1. 非空校验
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 2. 校验文件大小
        long fileSize = file.getSize();
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 5 * ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过5MB");
        // 3. 校验文件格式
        String fileSuffix = FileUtil.getSuffix(file.getOriginalFilename());
        final List<String> ALLOW_FORMAT_LIAT = Arrays.asList("png", "jpg", "jpeg", "gif", "bmp", "tif", "tiff", "svg", "ico", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIAT.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件格式错误 ");
    }


    /**
     * 删除临时文件
     *
     * @param file 临时文件
     */
    public static void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean deleted = file.delete();
        if (!deleted) {
            log.error("file delete error, filePath={}", file.getAbsolutePath());
        }
    }
}




