package com.mogo.project; // ⚠️ 请修改为您实际的根包名


import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;


@Slf4j
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
// ⚠️ 关键配置：MyBatis Plus 扫描路径。
// 请确保这里的路径指向您存放 Mapper 接口的包 (例如 com.yourcompany.project.mapper)
@MapperScan("com.mogo.project.**.mapper")
@EnableAsync //开启多线程
@EnableCaching // 开启缓存注解支持
@EnableScheduling // 开启定时任务（用于归档清理）
public class MogoApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(com.mogo.project.MogoApplication.class, args);

        // 打印启动成功的日志，方便点击跳转 Swagger 文档
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port", "8080");
        String path = env.getProperty("server.servlet.context-path", "");

        log.info("\n----------------------------------------------------------\n\t" +
                        "应用 '{}' 运行成功! \n\t" +
                        "访问地址:\n\t" +
                        "Local: \t\thttp://localhost:{}{}\n\t" +
                        "External: \thttp://{}:{}{}\n\t" +
                        "API 文档: \thttp://localhost:{}{}/doc.html (或 /swagger-ui/index.html)\n" +
                        "----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                port, path,
                ip, port, path,
                port, path);
    }

}