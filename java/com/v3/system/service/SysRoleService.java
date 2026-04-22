package com.v3.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.v3.model.system.SysRole;
import com.v3.model.vo.AssginRoleVo;
import com.v3.model.vo.SysRoleQueryVo;

public interface SysRoleService extends IService<SysRole> {
    IPage<SysRole> selectPage(Page<SysRole> pageParam, SysRoleQueryVo roleQueryVo);

    void doAssign(AssginRoleVo assginRoleVo);

}
