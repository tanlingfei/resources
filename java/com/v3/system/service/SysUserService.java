package com.v3.system.service;

import com.v3.model.system.SysUser;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.v3.model.vo.SysPwdVo;
import com.v3.model.vo.SysUserQueryVo;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface SysUserService extends IService<SysUser> {

    IPage<SysUser> selectPage(Page<SysUser> pageParam, SysUserQueryVo adminQueryVo);

    public void updateStatus(String id, Integer status);

    public boolean save(SysUser sysUser);

    public boolean register(SysUser sysUser);

    public boolean updateById(SysUser sysUser, HttpServletRequest request);

    SysUser getByUsername(String username);

    /**
     * 根据用户名获取用户登录信息
     * @param username
     * @return
     */
    Map<String, Object> getUserInfo(String username);
    public SysUser getById(String id);

    public void changePwd(SysPwdVo sysPwdVo);
}
