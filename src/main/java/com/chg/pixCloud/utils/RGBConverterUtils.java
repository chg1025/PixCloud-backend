package com.chg.pixCloud.utils;

import com.chg.pixCloud.common.ErrorCode;
import com.chg.pixCloud.exception.BusinessException;

public class RGBConverterUtils {

    /**
     * 将5位RGB字符串转换为6位标准RGB字符串。
     * 如果输入已经是标准的6位RGB，则直接返回。
     *
     * @param rgb 输入的RGB字符串，例如 "0x080e0" 或 "0x0080E0"
     * @return 标准6位RGB字符串，例如 "0x0080E0"
     * @throws IllegalArgumentException 如果输入不符合RGB格式
     */
    public static String toStandardRGB(String rgb) {
        // 检查输入是否以 "0x" 开头
        if (rgb == null || (!rgb.startsWith("0x") && !rgb.startsWith("0X"))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片色彩格式错误");
        }

        // 提取去掉 "0x" 的部分
        String hexPart = rgb.substring(2);

        // 根据长度判断处理
        if (hexPart.length() == 5) {
            // 如果是5位，补齐为6位
            return String.format("0x%06X", Integer.parseInt(hexPart, 16));
        } else if (hexPart.length() == 6) {
            // 如果是6位，直接返回
            return rgb;
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片色彩格式错误");
        }
    }

}
