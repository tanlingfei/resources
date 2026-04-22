package com.v3.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.v3.model.system.SysUserRole;
import com.v3.system.mapper.SysUserRoleMapper;
import com.v3.system.service.SysUserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

}
