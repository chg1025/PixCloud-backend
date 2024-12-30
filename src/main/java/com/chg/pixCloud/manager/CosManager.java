package com.chg.pixCloud.manager;

import com.chg.pixCloud.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

@Component
public class CosManager {
    @Resource
    CosClientConfig cosClientConfig;
    @Resource
    COSClient cosClient;

    /**
     * 上传对象到COS
     *
     * @param key  唯一键（路径）
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 从COS下载对象
     *
     * @param key 对象的唯一键（路径）
     * @return cos对象
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传图片到COS，并解析
     *
     * @param key  唯一键（路径）
     * @param file 文件
     */
    public PutObjectResult putAndOptionPictureToCOS(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        // 获取图片基本信息（获取图片基本信息文档中视为图片的基本处理）
        /*
            文档：https://cloud.tencent.com/document/product/436/55378
                 https://cloud.tencent.com/document/product/436/113308
        */
        PicOperations picOperations = new PicOperations();
        // 设置返回原图信息
        picOperations.setIsPicInfo(1);
        // 构造处理参数
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }
}
