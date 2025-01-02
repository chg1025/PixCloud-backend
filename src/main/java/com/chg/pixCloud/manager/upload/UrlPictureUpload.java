package com.chg.pixCloud.manager.upload;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.chg.pixCloud.common.ErrorCode;
import com.chg.pixCloud.exception.BusinessException;
import com.chg.pixCloud.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * URL图片上传
 */
@Slf4j
@Service
public class UrlPictureUpload extends PictureUploadTemplate {

    /**
     * 校验输入源（URL）
     *
     * @param inputSource 文件输入源
     */
    @Override
    protected void validPicture(Object inputSource) {
        String url = (String) inputSource;
        // 1. 非空校验
        ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
        // 2. 校验url格式
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }
        // 3. 发送HEAD请求验证文件是否存在
        try (HttpResponse httpResponse = HttpUtil.createRequest(Method.HEAD, url).execute()) {
            // 未正常响应，直接返回，无需执行其他操作
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 4. 文件存在，文件类型校验
            String contentType = httpResponse.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                final List<String> ALLOW_CONTENT_TYPE = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/svg", "image/ico", "image/webp", "text/html");
                log.info("文件类型: {}", contentType);
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPE.contains(contentType.toLowerCase()), ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 5. 文件存在，文件大小校验
            String contentLengthStr = httpResponse.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long ONE_MB = 1024 * 1024;
                    ThrowUtils.throwIf(contentLength > 5 * ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过 5MB");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式异常");
                }
            }

        }
    }

    /**
     * 获取输入源的原始文件名
     *
     * @param inputSource 文件输入源
     * @return 文件名
     */
    @Override
    protected String getOriginalFilename(Object inputSource) {
        // 移除 URL 查询参数
        String url = URLUtil.getPath((String) inputSource);
        // 图片名称默认是default, 图片后缀默认是webp
        String imgName = "default";
        String extName = "webp";
        try {
            imgName = FileUtil.mainName(url);
            // 使用 HuTool FileTypeUtil 获取文件类型
            InputStream inputStream = new URL((String) inputSource).openStream();
            extName = FileTypeUtil.getType(inputStream);
        } catch (Exception e) {
            log.error("图片后缀获取失败「{}」", url);
        }
        // 返回图片名称及后缀
        return imgName + "." + extName;
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
            HttpUtil.downloadFile((String) inputSource, file);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件获取到服务器失败");
        }
    }
}
