package com.v3.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.v3.model.system.SysRoleMenu;
import com.v3.system.mapper.SysRoleMenuMapper;
import com.v3.system.service.SysRoleMenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

}
