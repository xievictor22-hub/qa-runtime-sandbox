package com.mogo.project.modules.quote.domain.order.service.component;


import com.mogo.project.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

/**
 * 根据公式计算出结果H*W/1000000*1.0800
 */
@Component
@Slf4j
public class FormulaCalculator {

    // 解析器是线程安全的，可以复用
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 动态计算公式，只计算有参数的
     * @param formulaStr 数据库里的公式，如 "H * W / 1000000 * 1.08"
     * @param h 高/长 (对应 QuoteDetail.length)
     * @param w 宽 (对应 QuoteDetail.width)
     * @return 计算结果
     */
    public BigDecimal calculate(String formulaStr, BigDecimal h, BigDecimal w) {
        if (formulaStr == null || formulaStr.isBlank()) {
            log.error("底价公式为空计算失败: " );
            throw new ServiceException("底价公式为空计算失败");
        }

        try {
            // 1. 准备上下文变量
            HashMap<String, BigDecimal> params = new HashMap<>();
            // 设置变量别名 (不区分大小写通常比较友好，但SpEL默认区分)
            // 这里的 value 即使是 null 也要处理，防止空指针，建议给默认值 0
            params.put("H", h != null ? h : BigDecimal.ZERO);
            params.put("W", w != null ? w : BigDecimal.ZERO);
            params.put("h", h != null ? h : BigDecimal.ZERO); // 兼容小写
            params.put("w", w != null ? w : BigDecimal.ZERO);
            //todo 解决计算错误:底价公式计算失败: H*W/1000000*1.08 的问题
            // 2. 解析并计算
            // SpEL 会自动进行 BigDecimal 的运算，但除法需要注意精度
            // 原始公式: H*W/1000000*1.0800

            StandardEvaluationContext context = new StandardEvaluationContext(params);
            context.addPropertyAccessor(new MapAccessor());
            // 为了保证除法不报错 (Non-terminating decimal expansion)，
            // 建议在公式层面或代码层面控制精度。
            // SpEL 默认行为对 BigDecimal 比较智能，但复杂的连除建议用 T(java.math.BigDecimal) 处理
            BigDecimal result = parser.parseExpression(formulaStr).getValue(context, BigDecimal.class);

            // 3. 最终结果保留 2 位小数 (根据业务需求调整)
            return result != null ? result.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        } catch (Exception e) {
            // 记录日志，公式配置错误不应卡死流程，可返回0或抛出特定异常
            log.error("底价公式计算失败: {}" , formulaStr,e);
            throw new ServiceException("底价公式计算失败: " + formulaStr);
        }
    }

    /**
     * 动态计算公式,只计算无参数的
     * @param formulaStr 数据库里的公式，
     * @return 计算结果
     */
    public BigDecimal calculate(String formulaStr) {
        if (formulaStr == null || formulaStr.isBlank()) {
            log.error("公式为空计算失败: " );
            throw new ServiceException("公式为空计算失败");
        }
        try {
            BigDecimal result = parser.parseExpression(formulaStr).getValue( BigDecimal.class);
            // 3. 最终结果保留 2 位小数 (根据业务需求调整)
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            // 记录日志，公式配置错误不应卡死流程，可返回0或抛出特定异常
            log.error("公式计算失败: {}" , formulaStr);
            throw new ServiceException("公式计算失败: " + formulaStr);
        }
    }
}
