package com.mogo.project;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.nio.file.Paths;
import java.util.Collections;

/**
 * 代码生成器 (运行 main 方法即可)
 */
public class CodeGenerator {

    public static void main(String[] args) {
        // 1. 数据库配置 (修改为你的数据库账号密码)
        String url = "jdbc:mysql://localhost:3306/mogo_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "123";

        // 获取项目绝对路径 (确保代码生成在当前项目下)
        String projectPath = System.getProperty("user.dir");
        // 如果是多模块项目，可能需要加上子模块路径，例如: + "/backend"
        // String projectPath = System.getProperty("user.dir") + "/backend";

        FastAutoGenerator.create(url, username, password)
                // 2. 全局配置
                .globalConfig(builder -> {
                    builder.author("Gemini") // 设置作者
                            .enableSwagger() // 开启 swagger 模式 (生成 @Schema 注解)
                            .dateType(DateType.TIME_PACK) // 使用 Java 8 时间类型 (LocalDateTime)
                            .commentDate("yyyy-MM-dd")
                            .outputDir(projectPath + "/src/main/java"); // 指定输出目录
                })
                // 3. 包配置
                .packageConfig(builder -> {
                    builder.parent("com.mogo.project.modules") // 设置父包名
                            .moduleName("system") // 设置模块名 (生成的类会在 com.mogo.project.system 下)
                            .entity("model.entity") // Entity 包名
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            .mapper("mapper")
                            .xml("mapper")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, projectPath + "/src/main/resources/mapper/system")); // XML 生成路径
                })

                // 4. 策略配置 (核心)
                .strategyConfig(builder -> {
                    builder.addInclude("sys_dict_data") // 【关键】设置需要生成的表名 (支持多个: "sys_user", "sys_role")
                            .addTablePrefix() // 设置过滤表前缀 (生成类名时不带 sys_)

                            // Entity 策略
                            .entityBuilder()
                            .enableLombok() // 开启 Lombok
                            .enableTableFieldAnnotation() // 开启字段注解
                            .enableFileOverride() // 允许覆盖旧文件 (慎用)

                            // Controller 策略
                            .controllerBuilder()
                            .enableRestStyle() // 开启 @RestController

                            // Service 策略
                            .serviceBuilder()
                            .formatServiceFileName("I%sService") // 接口名称前加 I

                            // Mapper 策略
                            .mapperBuilder()
                            .enableMapperAnnotation(); // 开启 @Mapper 注解
                })
                .templateEngine(new VelocityTemplateEngine()) // 使用 Velocity 引擎
                .execute();

        System.out.println("代码生成完毕！");
    }
}