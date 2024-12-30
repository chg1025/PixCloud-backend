package com.chg.pixCloud.controller;

import com.chg.pixCloud.annotation.AuthCheck;
import com.chg.pixCloud.common.BaseResponse;
import com.chg.pixCloud.common.ErrorCode;
import com.chg.pixCloud.constant.UserConstant;
import com.chg.pixCloud.exception.BusinessException;
import com.chg.pixCloud.manager.CosManager;
import com.chg.pixCloud.utils.ResultUtils;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {
    @Resource
    CosManager cosManager;

    /**
     * 测试文件上传（管理员）
     *
     * @param multipartFile 上传文件
     * @return
     */
    @PostMapping("/test/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_USER)
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        // 1. 文件名
        String fileName = multipartFile.getOriginalFilename();
        // 2. 上传文件目录
        String filePath = String.format("/test/upload/%s", fileName);

        File file = null;
        try {
            // 3. 上传文件
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath, file);
            // 4. 返回可访问文件的地址
            return ResultUtils.success(filePath);
        } catch (Exception e) {
            log.error("file upload error, filePath={}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // finally 删除临时文件
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filePath={}", filePath);
                }

            }
        }
    }

    /**
     * 文件下载测试
     *
     * @param filePath 文件路径
     * @param response 响应对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_USER)
    @GetMapping("/test/download/")
    public void testDownloadFile(String filePath, HttpServletResponse response) throws Exception {
        COSObjectInputStream cosObjectInputStream = null;
        try {
            COSObject cosObject = cosManager.getObject(filePath);
            cosObjectInputStream = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInputStream);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filePath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("download file error, filePath={}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            // 关闭流
            if (cosObjectInputStream != null) {
                cosObjectInputStream.close();
            }
        }
    }

}
