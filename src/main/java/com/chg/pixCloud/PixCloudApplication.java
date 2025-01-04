package com.chg.pixCloud;

import com.zaxxer.hikari.HikariDataSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


@SpringBootApplication
@EnableAsync
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 2592000) // 启用 Spring Session，30 天
@MapperScan("com.chg.pixCloud.mapper")
// 当 exposeProxy = true 时，Spring AOP 会将当前的 AOP 代理暴露到一个线程上下文中。这意味着在当前线程中，可以通过 AopContext.currentProxy() 方法获取当前代理对象。
@EnableAspectJAutoProxy(exposeProxy = true)
public class PixCloudApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(PixCloudApplication.class, args);
        HikariDataSource hikariDataSource = applicationContext.getBean(HikariDataSource.class);
        System.out.println(hikariDataSource.getHikariPoolMXBean().getActiveConnections());

    }
}
