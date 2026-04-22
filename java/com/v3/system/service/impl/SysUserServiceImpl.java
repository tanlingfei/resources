package com.v3.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.v3.common.result.ResultCodeEnum;
import com.v3.common.utils.MD5;
import com.v3.model.system.SysUser;
import com.v3.model.system.SysUserRole;
import com.v3.model.vo.SysPwdVo;
import com.v3.model.vo.SysUserQueryVo;
import com.v3.system.exception.LanfException;
import com.v3.system.mapper.SysUserMapper;
import com.v3.system.service.FileService;
import com.v3.system.service.SysMenuService;
import com.v3.system.service.SysUserRoleService;
import com.v3.system.service.SysUserService;
import com.v3.system.utils.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;

@Transactional
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private FileService fileService;

    @Override
    public IPage<SysUser> selectPage(Page<SysUser> pageParam, SysUserQueryVo userQueryVo) {
        //只查询当前登录所属部门数据
        SysUser sysUser = UserUtil.getUserInfo();
        return sysUserMapper.selectPage(pageParam, userQueryVo);
    }

    @Override
    public void updateStatus(String id, Integer status) {
        SysUser sysUser = sysUserMapper.selectById(id);
        sysUser.setStatusData(sysUser.getStatus() ? 1 : 0);
        sysUserMapper.updateById(sysUser);
    }

    @Override
    public SysUser getByUsername(String username) {
        SysUser sysUser = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("username", username));
        return sysUser;
    }

    @Override
    public boolean save(SysUser sysUser) {
        if ("".equals(sysUser.getPassword()) || "null".equals(sysUser.getPassword())) {
            String pwd = MD5.encrypt("123456");
            sysUser.setPassword(pwd);
        } else {
            sysUser.setPassword(MD5.encrypt(sysUser.getPassword()));
        }
        sysUser.setStatusData(sysUser.getStatus() ? 1 : 0);
        int result = this.sysUserMapper.insert(sysUser);
        List<String> roleList = sysUser.getRoleList();
        if (roleList != null && roleList.size() > 0) {
            List<SysUserRole> saveRoles = new ArrayList<>();
            for (String roleId : roleList) {
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setUserId(sysUser.getId());
                sysUserRole.setRoleId(roleId);
                saveRoles.add(sysUserRole);
            }
            this.sysUserRoleService.saveBatch(saveRoles);
        }
        return result > 0;
    }

    @Override
    public boolean register(SysUser sysUser) {
        String pwd = MD5.encrypt(sysUser.getPassword());
        sysUser.setPassword(pwd);
        sysUser.setStatusData(1);
        int result = this.sysUserMapper.insert(sysUser);
        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setUserId(sysUser.getId());
        //注册用户默认赋予普通用户角色
        sysUserRole.setRoleId("1793660824038866945");
        sysUserRoleService.save(sysUserRole);
        return result > 0;
    }

    @Override
    public boolean updateById(SysUser sysUser, HttpServletRequest request) {
        if(sysUser.getId() == null || sysUser.getId().equals("")){
            sysUser.setId(UserUtil.getUserId());
        }
        MultipartFile file = sysUser.getFile();
        String filePath = null;
        if (file != null) {
            try {
                filePath = fileService.upload(file,"hghfghdf");
            } catch (Exception e) {
                e.printStackTrace();
                throw new LanfException(ResultCodeEnum.UPLOAD_ERROR);
            }
        }
        SysUser oldUser = this.getById(sysUser.getId());
        if (filePath != null) {
            sysUser.setHeadUrl("http://" + request.getServerName() + ":" + request.getServerPort() + filePath);
        } else {
            sysUser.setHeadUrl(oldUser.getHeadUrl());
        }
        if (StringUtils.isNotBlank(sysUser.getPassword()) && !oldUser.getPassword().equals(sysUser.getPassword())) {
            String pwd = MD5.encrypt(sysUser.getPassword());
            sysUser.setPassword(pwd);
        }
        if (sysUser.getStatus() == null) {
            sysUser.setStatusData(1);
        } else {
            sysUser.setStatusData(sysUser.getStatus() ? 1 : 0);
        }
        int row = this.sysUserMapper.updateById(sysUser);
        List<String> roleList = sysUser.getRoleList();
        if (roleList != null && roleList.size() > 0) {
            List<SysUserRole> saveRoles = new ArrayList<>();
            for (String roleId : roleList) {
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setUserId(sysUser.getId());
                sysUserRole.setRoleId(roleId);
                saveRoles.add(sysUserRole);
            }
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("user_id", sysUser.getId());
            sysUserRoleService.remove(queryWrapper);
            this.sysUserRoleService.saveBatch(saveRoles);
        }
        return row > 0;
    }

    @Override
    public SysUser getById(String id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        sysUser.setStatus("1".equals(sysUser.getStatusData().toString()));
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("role_id");
        queryWrapper.eq("user_id", sysUser.getId());
        Function<Object, String> f = (o -> o.toString());
        List<String> roleList = sysUserRoleService.listObjs(queryWrapper, f);
        sysUser.setRoleList(roleList);
        return sysUser;
    }

    @Override
    public Map<String, Object> getUserInfo(String username) {
        Map<String, Object> result = new HashMap<>();
        SysUser sysUser = this.getByUsername(username);
        if (sysUser != null) {
            result.put("username", sysUser.getUsername());
            result.put("id", sysUser.getId());
            result.put("name", sysUser.getName());
            result.put("mobile", sysUser.getMobile());
            result.put("email", sysUser.getEmail());
            result.put("address", sysUser.getAddress());
            result.put("avatar", "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
            result.put("roles", new HashSet<>());
            List<String> buttons = sysMenuService.findUserPermsList(sysUser.getId());
            result.put("buttons", buttons);
        }
        return result;
    }

    @Override
    public void changePwd(SysPwdVo sysPwdVo) {
        SysUser sysUser = this.getById(UserUtil.getUserId());
        if (!MD5.encrypt(sysPwdVo.getPassword()).equals(sysUser.getPassword())) {
            throw new LanfException(5240, "旧密码错误");
        }
        if (!sysPwdVo.getCfpassword().equals(sysPwdVo.getNpassword())) {
            throw new LanfException(5240, "两次密码不一致");
        }
        sysUser.setPassword(MD5.encrypt(sysPwdVo.getCfpassword()));
        this.updateById(sysUser);
    }
}
