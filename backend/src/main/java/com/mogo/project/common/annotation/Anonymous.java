package com.mogo.project.common.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/*
  POST 请求 /system/user -> 自动映射为权限 system:user:add
  PUT 请求 /system/user -> 自动映射为权限 system:user:edit
  DELETE 请求 /system/user/{id} -> 自动映射为权限 system:user:remove
  GET 请求 /system/user/list -> 自动映射为权限 system:user:query
  若有该注解，则AutoPermissionAspect不会自动配置，而是需要手动配置
 */
public @interface Anonymous {
}