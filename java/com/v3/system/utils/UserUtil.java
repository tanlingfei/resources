package com.v3.system.utils;

import com.alibaba.fastjson.JSON;
import com.v3.common.helper.JwtHelper;
import com.v3.common.utils.BeanUtil;
import com.v3.common.utils.WebUtil;
import com.v3.model.system.SysUser;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tanlingfei
 * @version 1.0
 * @description TODO
 * @date 2023/4/27 15:53
 */
public class UserUtil {

    public static String getUserId() {
        HttpServletRequest request = WebUtil.getRequest();
        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            token = request.getParameter("token");
        }
        String userId = JwtHelper.getUserIds(token);
        return userId;
    }

    public static SysUser getUserInfo() {
        String userId = getUserId();
        RedisTemplate redisTemplate = (RedisTemplate) BeanUtil.getBean("redisTemplate");
        String result = (String) redisTemplate.opsForValue().get(userId);
        if (result != null) {
            return JSON.parseObject(result, SysUser.class);
        }
        return null;
    }

}
