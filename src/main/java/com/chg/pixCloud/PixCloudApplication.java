package com.chg.pixCloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@SpringBootApplication
@MapperScan("com.chg.pixCloud.mapper")
// 当 exposeProxy = true 时，Spring AOP 会将当前的 AOP 代理暴露到一个线程上下文中。这意味着在当前线程中，可以通过 AopContext.currentProxy() 方法获取当前代理对象。
@EnableAspectJAutoProxy(exposeProxy = true)
public class PixCloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(PixCloudApplication.class, args);
    }
}
