package com.v3.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.v3.model.system.SysRole;
import com.v3.model.system.SysUserRole;
import com.v3.model.vo.AssginRoleVo;
import com.v3.model.vo.SysRoleQueryVo;
import com.v3.system.mapper.SysRoleMapper;
import com.v3.system.service.SysRoleService;
import com.v3.system.service.SysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Override
    public void doAssign(AssginRoleVo assginRoleVo) {
        //根据用户id删除原来分配的角色
        QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", assginRoleVo.getUserId());
        sysUserRoleService.remove(queryWrapper);
        //获取所有的角色id
        List<String> roleIdList = assginRoleVo.getRoleIdList();
        List<SysUserRole> saveUserRoles = new ArrayList<>();
        for (String roleId : roleIdList) {
            if (roleId != null) {
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setUserId(assginRoleVo.getUserId());
                sysUserRole.setRoleId(roleId);
                saveUserRoles.add(sysUserRole);
            }
        }
        if (saveUserRoles.size() > 0) {
            sysUserRoleService.saveBatch(saveUserRoles);
        }
    }

    @Override
    public IPage<SysRole> selectPage(Page<SysRole> pageParam, SysRoleQueryVo roleQueryVo) {
        return sysRoleMapper.selectPage(pageParam, roleQueryVo);
    }

}
