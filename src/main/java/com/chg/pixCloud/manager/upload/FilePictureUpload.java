package com.chg.pixCloud.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.chg.pixCloud.common.ErrorCode;
import com.chg.pixCloud.exception.BusinessException;
import com.chg.pixCloud.utils.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 文件图片上传
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {

    /**
     * 校验输入源（本地文件）
     *
     * @param inputSource 文件输入源
     */
    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile file = (MultipartFile) inputSource;
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
     * 获取输入源的原始文件名
     *
     * @param inputSource 文件输入源
     * @return 文件名
     */
    @Override
    protected String getOriginalFilename(Object inputSource) {
        return ((MultipartFile) inputSource).getOriginalFilename();
    }

    /**
     * 处理文件输入源生成本地临时文件
     *
     * @param inputSource 文件输入源
     * @param file        本地临时文件地址
     */
    @Override
    protected void processFIle(Object inputSource, File file) {
        try {
            ((MultipartFile) inputSource).transferTo(file);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "获取文件到服务器失败");
        }
    }
}
