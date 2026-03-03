package com.mogo.project.common.aspect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.mogo.project.common.annotation.Log;
import com.mogo.project.common.util.IpUtils;
import com.mogo.project.modules.auth.model.LoginUser;
import com.mogo.project.modules.system.model.entity.SysOperLog;
import com.mogo.project.modules.system.service.SysOperLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils; // 需要引入 commons-lang3
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.aspectj.lang.annotation.Before; // 记得导入 Before
import org.springframework.core.NamedThreadLocal; // 可选，Spring提供的带有命名的ThreadLocal

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
/**
 * 添加系统级日志
 */
public class LogAspect {

    private final SysOperLogService sysOperLogService;

    /** 排除敏感属性字段 */
    public static final String[] EXCLUDE_PROPERTIES = { "password", "oldPassword", "newPassword", "confirmPassword"
            ,"token", "refreshToken","Authorization","captcha","code"};

    /**
     * 计算操作消耗时间
     * 使用 NamedThreadLocal 方便调试，也可以直接用 new ThreadLocal<>()
     */
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<>("Cost-Time");

    /**
     * 处理请求前执行：记录开始时间
     */
    @Before(value = "@annotation(controllerLog)")
    public void boBefore(JoinPoint joinPoint, Log controllerLog) {
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }

    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            // 获取当前请求对象
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            // 获取当前用户
            LoginUser loginUser = getLoginUser();

            // 构建日志对象
            SysOperLog operLog = new SysOperLog();
            operLog.setStatus(1); // 默认为成功
            operLog.setOperIp(IpUtils.getIpAddr(request)); // ★ 使用真实IP
            operLog.setOperUrl(request.getRequestURI());

            if (loginUser != null) {
                operLog.setOperName(loginUser.getUsername());
                operLog.setDeptName(loginUser.getSysUser().getDept() != null ? loginUser.getSysUser().getDept().getDeptName() : "未知");
            } else {
                operLog.setOperName("未知/未登录");
            }

            if (e != null) {
                operLog.setStatus(0); // 异常状态
                operLog.setErrorMsg(e.getMessage() != null && e.getMessage().length() > 2000 ?
                        e.getMessage().substring(0, 2000) : e.getMessage());
            }

            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");
            operLog.setRequestMethod(request.getMethod());
            operLog.setOperTime(LocalDateTime.now());
            // ★★★ 核心修改：计算耗时 ★★★
            // 1. 获取开始时间
            Long startTime = TIME_THREADLOCAL.get();
            // 2. 计算差值 (当前时间 - 开始时间)
            if (startTime != null) {
                operLog.setCostTime(System.currentTimeMillis() - startTime);
                // 3. 【非常重要】用完必须移除，防止内存泄漏 (尤其在线程池环境下)
                TIME_THREADLOCAL.remove();
            } else {
                operLog.setCostTime(0L);
            }
            // 处理注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, operLog, jsonResult, request);

            // 异步保存
            sysOperLogService.recordOper(operLog);
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("==前置通知异常==");
            log.error("异常信息:{}", exp.getMessage());
            TIME_THREADLOCAL.remove();
            exp.printStackTrace();
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, Log log, SysOperLog operLog, Object jsonResult, HttpServletRequest request) {
        // 设置标题
        operLog.setTitle(log.title());
        // 设置业务类型
        operLog.setBusinessType(log.businessType().ordinal());

        // 是否需要保存请求数据
        if (log.isSaveRequestData()) {
            setRequestValue(joinPoint, operLog, log.excludeParamNames(), request);
        }

        // 是否需要保存响应数据
        if (log.isSaveResponseData() && jsonResult != null) {
            operLog.setJsonResult(JSON.toJSONString(jsonResult));
        }
    }

    /**
     * 获取请求的参数，放到log中
     */
    private void setRequestValue(JoinPoint joinPoint, SysOperLog operLog, String[] excludeParamNames, HttpServletRequest request) {
        String requestMethod = operLog.getRequestMethod();
        if (HttpMethod.PUT.name().equals(requestMethod) || HttpMethod.POST.name().equals(requestMethod)) {
            // 获取参数
            String params = argsArrayToString(joinPoint.getArgs(), excludeParamNames);
            operLog.setOperParam(params != null && params.length() > 2000 ? params.substring(0, 2000) : params);
        } else {
            // GET/DELETE 请求，也可以记录 QueryString
             String params = request.getQueryString();
             operLog.setOperParam(params);
        }
    }

    /**
     * ★★★ 核心修复：参数拼装 ★★★
     * 过滤掉不能序列化的对象
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                // 1. 过滤 BindingResult, HttpRequest, HttpResponse
                if (o != null && !isFilterObject(o)) {
                    try {
                        // 2. 序列化为 JSON，并过滤敏感字段
                        String jsonObj = JSON.toJSONString(o, excludePropertyPreFilter(excludeParamNames));
                        params.append(jsonObj).append(" ");
                    } catch (Exception e) {
                        // 忽略序列化失败的参数
                    }
                }
            }
        }
        return params.toString().trim();
    }

    /**
     * 判断是否需要过滤的对象
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.entrySet()) {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }

    /**
     * 忽略敏感属性
     */
    public PropertyFilter excludePropertyPreFilter(String[] excludeParamNames) {
        return (source, name, value) -> {
            // 默认排除密码
            if (ArrayUtils.contains(EXCLUDE_PROPERTIES, name)) {
                return false;
            }
            // 排除注解中自定义的字段
            if (excludeParamNames != null && excludeParamNames.length > 0) {
                return !ArrayUtils.contains(excludeParamNames, name);
            }
            return true;
        };
    }

    /**
     * 获取当前登录用户 (安全获取)
     */
    private LoginUser getLoginUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser) {
                return (LoginUser) auth.getPrincipal();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}