package com.mogo.project.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.math.BigInteger;

@Configuration
/**
 * 对前端传入的数据格式进行转换
 */
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // 1. 将 Long 类型序列化为 String (解决 JS 精度丢失)

            // 针对 Long 类型（包装类）启用自定义序列化
            builder.serializerByType(Long.class, bigNumberSerializer());
            // 针对 long 类型（基本类型）也启用（MyBatis Plus 的 Page 对象中使用的是 long）
            builder.serializerByType(Long.TYPE, bigNumberSerializer());

            // 2. (可选) 如果你有 LocalDateTime 格式化需求也可以在这里配置
            // builder.modules(new JavaTimeModule());
        };
    }

    /**
     * 自定义序列化逻辑：
     * 只有超过 JS 最大安全整数 (2^53 - 1) 的数值才转字符串，
     * 其他小数值（如分页信息）保持数字格式。
     */
    private JsonSerializer<Long> bigNumberSerializer() {
        return new JsonSerializer<Long>() {
            @Override
            public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value != null) {
                    // JS 最大安全整数约为 9007199254740991 (16位)
                    // 雪花算法 ID 通常是 19 位，所以设定阈值 > 16位 即可，或者直接判断数值
                    if (value > 9007199254740991L || value < -9007199254740991L) {
                        gen.writeString(value.toString());
                    } else {
                        gen.writeNumber(value);
                    }
                } else {
                    gen.writeNull();
                }
            }
        };
    }
}