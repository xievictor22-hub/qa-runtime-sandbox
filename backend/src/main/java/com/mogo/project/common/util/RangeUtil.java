package com.mogo.project.common.util;



import lombok.Data;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 负责将【1，2】转成min max min-include max-include
 */
public class RangeUtil {
    // 正则支持 (0,4] 或 [100,100] 或 (0.5, 1.2]
    private static final Pattern RANGE_PATTERN = Pattern.compile("^([\\[\\(])\\s*(\\d+(?:\\.\\d+)?)\\s*,\\s*(\\d+(?:\\.\\d+)?)\\s*([\\]\\)])$");

    @Data
    public static class RangeResult {
        private BigDecimal min;
        private BigDecimal max;
        private Integer minInclude; // 1=[, 0=(
        private Integer maxInclude; // 1=], 0=)
        private boolean isValid = false;
    }

    public static RangeResult parse(String rangeStr) {
        RangeResult result = new RangeResult();
        if (rangeStr == null || rangeStr.trim().isEmpty()) {
            return result; // isValid=false, 数据库存NULL
        }

        Matcher matcher = RANGE_PATTERN.matcher(rangeStr.trim());
        if (matcher.find()) {
            String startBracket = matcher.group(1);
            result.setMin(new BigDecimal(matcher.group(2)));
            result.setMax(new BigDecimal(matcher.group(3)));
            result.setMinInclude("[".equals(startBracket) ? 1 : 0);
            result.setMaxInclude("]".equals(matcher.group(4)) ? 1 : 0);
            result.setValid(true);
        }
        return result;
    }
}