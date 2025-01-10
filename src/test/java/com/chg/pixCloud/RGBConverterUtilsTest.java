package com.chg.pixCloud;

import com.chg.pixCloud.exception.BusinessException;
import com.chg.pixCloud.utils.RGBConverterUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RGBConverterUtilsTest {
    @Test
    void testToStandardRGB_5DigitInput() {
        // 测试5位RGB转换
        String input = "0x080e0";
        String expected = "0x0080E0";
        assertEquals(expected, RGBConverterUtils.toStandardRGB(input));
    }

    @Test
    void testToStandardRGB_6DigitInput() {
        // 测试6位标准RGB
        String input = "0x0080E0";
        String expected = "0x0080E0";
        assertEquals(expected, RGBConverterUtils.toStandardRGB(input));
    }

    @Test
    void testToStandardRGB_5DigitInputUpperCase() {
        // 测试5位大写输入
        String input = "0XABCDE";
        String expected = "0x0ABCDE";
        assertEquals(expected, RGBConverterUtils.toStandardRGB(input));
    }

    @Test
    void testToStandardRGB_InvalidLength() {
        // 测试非法长度输入
        String input = "0x123"; // 少于5位
        Exception exception = assertThrows(BusinessException.class, () -> RGBConverterUtils.toStandardRGB(input));
        assertTrue(exception.getMessage().contains("图片色彩格式错误"));
    }

    @Test
    void testToStandardRGB_InvalidPrefix() {
        // 测试非 "0x" 或 "0X" 开头
        String input = "12345";
        Exception exception = assertThrows(BusinessException.class, () -> RGBConverterUtils.toStandardRGB(input));
        assertTrue(exception.getMessage().contains("图片色彩格式错误"));
    }

    @Test
    void testToStandardRGB_NullInput() {
        // 测试空输入
        String input = null;
        Exception exception = assertThrows(BusinessException.class, () -> RGBConverterUtils.toStandardRGB(input));
        assertTrue(exception.getMessage().contains("图片色彩格式错误"));
    }

    @Test
    void testToStandardRGB_InvalidHex() {
        // 测试非法十六进制字符
        String input = "0xXYZ12";
        Exception exception = assertThrows(NumberFormatException.class, () -> RGBConverterUtils.toStandardRGB(input));
    }
}
