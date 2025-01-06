package com.chg.pixCloud.api.imagesearch;

import com.chg.pixCloud.api.imagesearch.model.ImageSearchResult;
import com.chg.pixCloud.api.imagesearch.sub.GetImageFirstUrlApi;
import com.chg.pixCloud.api.imagesearch.sub.GetImageListApi;
import com.chg.pixCloud.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 以图搜图接口，门面模式封装
 */
@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param imageUrl 图片地址
     * @return 图片搜索结果列表
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        // 测试以图搜图功能  
        String imageUrl = "https://img95.699pic.com/photo/50059/8720.jpg_wh860.jpg";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表" + resultList);
    }
}
