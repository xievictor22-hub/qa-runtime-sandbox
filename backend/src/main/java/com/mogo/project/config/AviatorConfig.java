package com.mogo.project.config;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Options;
import jakarta.annotation.PostConstruct; // Spring Boot 3 使用 jakarta
// import javax.annotation.PostConstruct; // Spring Boot 2 使用 javax
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.math.MathContext;

@Slf4j
@Configuration
/**
 * 公式计算获取价格的配置
 */
public class AviatorConfig {

    @Value("${aviator.parse-decimal:true}") // 默认为 true
    private boolean parseDecimal;

    @Value("${aviator.math-context:DECIMAL128}") // 默认为 DECIMAL128
    private String mathContextType;

    /**
     * 项目启动时自动执行此方法进行配置
     */
    @PostConstruct
    public void initAviator() {
        log.info("正在初始化 Aviator 引擎配置...");

        // 1. 配置是否强制解析为 BigDecimal
        if (parseDecimal) {
            AviatorEvaluator.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
            log.info("Aviator: 已开启 [浮点数自动转 BigDecimal] 模式");
        }

        // 2. 配置 MathContext 精度
        MathContext context;
        switch (mathContextType.toUpperCase()) {
            case "DECIMAL32":
                context = MathContext.DECIMAL32;
                break;
            case "DECIMAL64":
                context = MathContext.DECIMAL64;
                break;
            case "UNLIMITED":
                context = MathContext.UNLIMITED;
                break;
            case "DECIMAL128":
            default:
                context = MathContext.DECIMAL128;
                break;
        }
        AviatorEvaluator.setOption(Options.MATH_CONTEXT, context);
        log.info("Aviator: 已设置计算精度为 [{}]", context);
    }
}