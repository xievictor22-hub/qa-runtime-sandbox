package com.mogo.project.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    /** 部门表的别名 (SQL中 d.dept_id) */
    String deptAlias() default "d";

    /** 用户表的别名 (SQL中 u.user_id) */
    String userAlias() default "u";

}