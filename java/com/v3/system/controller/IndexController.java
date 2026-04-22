package com.v3.system.controller;

import com.v3.common.helper.JwtHelper;
import com.v3.common.result.Result;
import com.v3.model.system.SysMenu;
import com.v3.model.system.SysUser;
import com.v3.model.vo.SysPwdVo;
import com.v3.system.service.SysMenuService;
import com.v3.system.service.SysUserService;
import com.v3.system.utils.UserUtil;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 后台登录登出
 * </p>
 */
@Api(tags = "后台登录管理")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysMenuService sysMenuService;


    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取用户信息
     *
     * @return
     */
    @GetMapping("/info")
    public Result info(HttpServletRequest request) {
        String username = JwtHelper.getUsername(request.getHeader("token"));
        Map<String, Object> map = sysUserService.getUserInfo(username);
        return Result.ok(map);
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    @GetMapping("/getInfo")
    public Result getInfo(HttpServletRequest request) {
        String userId = UserUtil.getUserId();
        SysUser sysUser = sysUserService.getById(userId);
        sysUser.setToken(request.getHeader("token"));
        return Result.ok(sysUser);
    }

    /**
     * 获取用户菜单权限
     *
     * @param request
     * @return
     */
    @GetMapping("/menuTree")
    public Result menuTree(HttpServletRequest request) {
        String username = JwtHelper.getUsername(request.getHeader("token"));
        List<SysMenu> menuList = sysMenuService.findUserMenuList(username);
        return Result.ok(menuList);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result logout() {
        SysUser sysUser = UserUtil.getUserInfo();
        redisTemplate.delete(sysUser.getUsername());
        redisTemplate.delete(sysUser.getId());
        return Result.ok();
    }

    @PostMapping("/changePwd")
    public Result changePwd(@RequestBody SysPwdVo sysPwdVo) {
        this.sysUserService.changePwd(sysPwdVo);
        return Result.ok();
    }

}
