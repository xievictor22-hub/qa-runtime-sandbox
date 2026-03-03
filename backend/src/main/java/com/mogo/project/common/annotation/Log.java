package com.mogo.project.common.annotation;

import com.mogo.project.common.enums.BusinessType;
import java.lang.annotation.*;

@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /** 模块标题 */
    String title() default "";

    /** 功能 */
    BusinessType businessType() default BusinessType.OTHER;

    /** 是否保存请求参数 */
    boolean isSaveRequestData() default true;

    /** 是否保存响应数据 */
    boolean isSaveResponseData() default true;

    /** 排除指定的请求参数 */
     String[] excludeParamNames() default {};
}