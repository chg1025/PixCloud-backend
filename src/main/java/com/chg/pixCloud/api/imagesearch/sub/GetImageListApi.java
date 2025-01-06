package com.chg.pixCloud.api.imagesearch.sub;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chg.pixCloud.api.imagesearch.model.ImageSearchResult;
import com.chg.pixCloud.common.ErrorCode;
import com.chg.pixCloud.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 获取图片列表（Step 3）
 */
@Slf4j
public class GetImageListApi {

    /**
     * 获取图片列表
     *
     * @param url 接口URL
     * @return 图片列表
     */
    public static List<ImageSearchResult> getImageList(String url) {
        try {
            // 发起GET请求  
            HttpResponse response = HttpUtil.createGet(url).execute();

            // 获取响应内容  
            int statusCode = response.getStatus();
            String body = response.body();

            // 处理响应  
            if (statusCode == 200) {
                // 解析 JSON 数据并处理  
                return processResponse(body);
            } else {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
        } catch (Exception e) {
            log.error("获取图片列表失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片列表失败");
        }
    }

    /**
     * 处理接口响应内容
     *
     * @param responseBody 接口返回的JSON字符串
     */
    private static List<ImageSearchResult> processResponse(String responseBody) {
        // 解析响应对象  
        JSONObject jsonObject = new JSONObject(responseBody);
        if (!jsonObject.containsKey("data")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (!data.containsKey("list")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }
        JSONArray list = data.getJSONArray("list");
        return JSONUtil.toList(list, ImageSearchResult.class);
    }

    public static void main(String[] args) {
        String url = "https://graph.baidu.com/ajax/pcsimi?carousel=503&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&inspire=general_pc&limit=30&next=2&render_type=card&session_id=5515762064326574016&sign=1269ee1fa5e80c47bccee01736172376&tk=bb605&tpl_from=pc";
        List<ImageSearchResult> imageList = getImageList(url);
        System.out.println("搜索成功" + imageList);
    }
}
