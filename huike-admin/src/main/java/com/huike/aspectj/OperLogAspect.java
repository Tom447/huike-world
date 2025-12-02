package com.huike.aspectj;

import com.alibaba.fastjson.JSONObject;
import com.huike.common.annotation.Log;
import com.huike.common.exception.CustomException;
import com.huike.domain.system.SysOperLog;
import com.huike.domain.system.dto.LoginUser;
import com.huike.service.ISysOperLogService;
import com.huike.utils.ip.AddressUtils;
import com.huike.utils.ip.IpUtils;
import com.huike.web.service.TokenService;
import io.lettuce.core.dynamic.annotation.CommandNaming;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;

/**
 * 记录日志
 */
@Slf4j
@Aspect
@Component
public class OperLogAspect {

    @Autowired
    private ISysOperLogService sysOperLogService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private TokenService tokenService;

    @AfterReturning(value = "execution(* com.huike.controller.*.*.*(..)) && @annotation(myLog)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Log myLog, Object result){  //正常
        handleLog(joinPoint, myLog, result , null); //记录日志
    }

    @AfterThrowing(value = "execution(* com.huike.controller.*.*.*(..)) && @annotation(myLog)", throwing = "throwable")
    public void afterThrowing(JoinPoint joinPoint, Log myLog, Throwable throwable){  //异常
        handleLog(joinPoint, myLog, null ,throwable); //记录日志
    }


    /*
    @Around("execution(* com.huike.controller.*.*.*(..)) && @annotation(myLog)")
    public Object recordLog(ProceedingJoinPoint joinPoint , Log myLog){
        Object result = null;
        try {
            result = joinPoint.proceed();//放行
            //记录正确日志
            handleLog(joinPoint, myLog, result , null); //记录日志
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            //记录异常错误日志
            handleLog(joinPoint, myLog, null ,throwable); //记录日志
            throw new CustomException(throwable.getMessage()); //继续往上抛
        }
        return result ;
    }*/

    //记录日志方法
    private void handleLog(JoinPoint joinPoint, Log myLog, Object result, Throwable throwable) {
        SysOperLog sysOperLog = new SysOperLog();

        sysOperLog.setTitle(myLog.title());
        sysOperLog.setBusinessType(myLog.businessType().ordinal());
        sysOperLog.setMethod(joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName());
        sysOperLog.setRequestMethod(request.getMethod()); //请求方式
        sysOperLog.setOperatorType(1);

        //当前登录用户信息
        LoginUser loginUser = tokenService.getLoginUser(request);
        if(loginUser != null){
            sysOperLog.setOperName(loginUser.getUser().getUserName());
            sysOperLog.setDeptName(loginUser.getUser().getDept() != null? loginUser.getUser().getDept().getDeptName():null);
        }

        sysOperLog.setOperUrl(request.getRequestURI());
        //IP
        String ipAddr = IpUtils.getIpAddr(request);
        sysOperLog.setOperIp(ipAddr);

        //IP归属地
        String location = AddressUtils.getRealAddressByIP(ipAddr);
        sysOperLog.setOperLocation(location);

        //参数, 返回值
        sysOperLog.setOperParam(Arrays.toString(joinPoint.getArgs()));
        sysOperLog.setJsonResult(JSONObject.toJSONString(result));
        sysOperLog.setOperTime(new Date());
        sysOperLog.setStatus(0);

        //异常判断
        if(throwable != null){
            sysOperLog.setStatus(1);
            sysOperLog.setErrorMsg(throwable.getMessage());
        }

        sysOperLogService.insertOperlog(sysOperLog);
    }

}
