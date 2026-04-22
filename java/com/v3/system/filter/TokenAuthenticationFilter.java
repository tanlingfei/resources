package com.v3.system.filter;

import com.alibaba.fastjson.JSON;
import com.v3.common.exception.CacheExpiredException;
import com.v3.common.helper.JwtHelper;
import com.v3.common.result.Result;
import com.v3.common.result.ResultCodeEnum;
import com.v3.common.utils.HeaderConstant;
import com.v3.common.utils.ResponseUtil;
import com.v3.system.exception.LanfException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author tanlingfei
 * @version 1.0
 * @description 认证解析过滤器
 * @date 2023/2/27 10:23
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private RedisTemplate redisTemplate;

    public TokenAuthenticationFilter(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        logger.info("uri:" + request.getRequestURI());
        //如果是登录接口，直接放行
        if ("/admin/system/index/login".equals(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }
        UsernamePasswordAuthenticationToken authentication = null;
        ResultCodeEnum TOKENEXPIRED = null;
        if ("/admin/system/index/menuTree".equals(request.getRequestURI())) {
            TOKENEXPIRED = ResultCodeEnum.TOKENEXPIREDBYMENU;
        } else {
            TOKENEXPIRED = ResultCodeEnum.TOKENEXPIRED;
        }
        try {
            authentication = getAuthentication(request);
        } catch (ExpiredJwtException e) {
            ResponseUtil.out(response, Result.build(null, TOKENEXPIRED));
            return;
        } catch (CacheExpiredException e) {
            ResponseUtil.out(response, Result.build(null, TOKENEXPIRED));
            return;
        } catch (LanfException e) {
            if (e.getCode().equals(ResultCodeEnum.LOGIN_AUTH.getCode())) {
                ResponseUtil.out(response, Result.build(null, ResultCodeEnum.LOGIN_AUTH));
                return;
            }
        }
        if (null != authentication) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } else {
            ResponseUtil.out(response, Result.build(null, TOKENEXPIRED));
            /* ResponseUtil.out(response, Result.build(null, ResultCodeEnum.PERMISSION));*/
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        // token置于header里
        String token = request.getHeader(HeaderConstant.token);
        if (StringUtils.isEmpty(token)) {
            token = request.getParameter(HeaderConstant.token);
        }
        if (StringUtils.isEmpty(token)) {
            throw new LanfException(ResultCodeEnum.LOGIN_AUTH);
        }
        logger.info("token:" + token);
        if (!StringUtils.isEmpty(token)) {
            String useruame = JwtHelper.getUsername(token);
            logger.info("useruame:" + useruame);
            if (!StringUtils.isEmpty(useruame)) {
                String authoritiesString = (String) redisTemplate.opsForValue().get(useruame);
                //String authoritiesString = (String) CacheMap.get(useruame);
                if (StringUtils.isEmpty(authoritiesString)) {
                    throw new CacheExpiredException(ResultCodeEnum.TOKENEXPIRED);
                    //ResponseUtil.out(response, Result.build(null, ResultCodeEnum.TOKENEXPIRED));
                }
                List<Map> mapList = JSON.parseArray(authoritiesString, Map.class);
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                for (Map map : mapList) {
                    authorities.add(new SimpleGrantedAuthority((String) map.get("authority")));
                }
                return new UsernamePasswordAuthenticationToken(useruame, null, authorities);
            }
        }
        return null;
    }
}
