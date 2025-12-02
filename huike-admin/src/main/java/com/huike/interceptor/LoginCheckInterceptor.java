package com.huike.interceptor;

import com.huike.common.constant.HttpStatus;
import com.huike.domain.system.dto.LoginUser;
import com.huike.utils.StringUtils;
import com.huike.web.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录校验拦截器
 */
@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenService tokenService;

    /**
     * @return  true : 放行 , false : 拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1. 获取请求头中jwt令牌
        String token = tokenService.getToken(request);

        //2. 校验令牌是否存在 , 如果不存在 , 响应 401
        if(StringUtils.isEmpty(token)){
            log.info("请求中携带的jwt令牌为空, 未登录");
            response.setStatus(HttpStatus.UNAUTHORIZED);
            return false;
        }

        //3. 校验令牌 , 如果非法 , 响应 401
        try {
            tokenService.parseToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("请求中携带的jwt令牌非法, 未登录");
            response.setStatus(HttpStatus.UNAUTHORIZED);
            return false;
        }

        //4. 获取redis中的用户登录信息 , 如果不存在 , 响应 401
        LoginUser loginUser = tokenService.getLoginUser(request);
        if(loginUser == null){
            log.info("用户登录信息已过期");
            response.setStatus(HttpStatus.UNAUTHORIZED);
            return false;
        }

        //5. 放行
        tokenService.refreshAndCacheToken(loginUser);
        return true;
    }
}
