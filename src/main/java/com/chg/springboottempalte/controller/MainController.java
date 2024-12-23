package com.chg.springboottempalte.controller;

import com.chg.springboottempalte.common.BaseResponse;
import com.chg.springboottempalte.utils.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检查
     * 可通过该接口，快速验证后端服务是否正常运行，所以该接口返回值非常简单
     * http:localhost:8080/api/health
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
