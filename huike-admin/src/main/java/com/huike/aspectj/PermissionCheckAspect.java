package com.huike.aspectj;

import com.huike.common.annotation.PreAuthorize;
import com.huike.common.constant.HttpStatus;
import com.huike.domain.system.dto.LoginUser;
import com.huike.web.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

@Slf4j
@Aspect
@Component
public class PermissionCheckAspect {

    @Autowired
    private TokenService tokenService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /**
     * 校验用户权限
     */
    @Around("execution(* com.huike.controller.*.*.*(..)) && @annotation(preAuthorize)")
    public Object checkPermission(ProceedingJoinPoint proceedingJoinPoint, PreAuthorize preAuthorize) throws Throwable {
        log.info("权限校验开始 ...");

        //1. 获取当前登录用户所有权限标识
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (loginUser == null || loginUser.getPermissions() == null){
            response.setStatus(HttpStatus.UNAUTHORIZED);
            return null;
        }

        //2. 获取方法上的注解的权限标识
        String methodPermission = preAuthorize.value();

        //3. 判断用户是否具有该权限, 如果没有,  响应401 .
        Set<String> permissions = loginUser.getPermissions();
        if(!permissions.contains(methodPermission) && !permissions.contains("*:*:*")){ //没有权限
            response.setStatus(HttpStatus.UNAUTHORIZED);
            return null;
        }

        //4. 放行
        return proceedingJoinPoint.proceed();
    }

}
