package com.v3.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.v3.common.result.ResultCodeEnum;
import com.v3.model.system.SysMenu;
import com.v3.model.system.SysRoleMenu;
import com.v3.model.system.SysUser;
import com.v3.model.vo.AssginMenuVo;
import com.v3.system.exception.LanfException;
import com.v3.system.mapper.SysMenuMapper;
import com.v3.system.service.SysMenuService;
import com.v3.system.service.SysRoleMenuService;
import com.v3.system.service.SysUserService;
import com.v3.system.utils.MenuHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Transactional
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    @Autowired
    private SysUserService sysUserService;

    @Override
    public List<SysMenu> findNodes() {
        //全部权限列表
        List<SysMenu> sysMenuList = sysMenuMapper.queryList("", "");
        if (CollectionUtils.isEmpty(sysMenuList)) return null;
        //构建树形数据
        List<SysMenu> result = MenuHelper.buildTree(sysMenuList);
        return result;
    }

    @Override
    public List<SysMenu> findDir(String notId) {
        List<SysMenu> sysMenuList = sysMenuMapper.queryList("0", notId);
        if (CollectionUtils.isEmpty(sysMenuList)) return null;
        //构建树形数据
        List<SysMenu> result = MenuHelper.buildTree(sysMenuList);
        return result;
    }

    @Override
    public List<SysMenu> findMenu() {
        List<SysMenu> sysMenuList = sysMenuMapper.queryList("1", "");
        if (CollectionUtils.isEmpty(sysMenuList)) return null;
      /*  //构建树形数据
        List<SysMenu> result = MenuHelper.buildTree(sysMenuList);*/
        return sysMenuList;
    }

    @Override
    public boolean removeById(Serializable id) {
        int count = this.count(new QueryWrapper<SysMenu>().eq("parent_id", id));
        if (count > 0) {
            throw new LanfException(ResultCodeEnum.NODE_ERROR);
        }
        sysMenuMapper.deleteById(id);
        return false;
    }


    @Override
    public List<String> findSysMenuByRoleId(String roleId) {
        //获取所有status为1的权限列表
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("menu_id");
        queryWrapper.eq("role_id", roleId);
        Function<Object, String> f = (o -> o.toString());
        return sysRoleMenuService.listObjs(queryWrapper, f);
    }

    @Override
    public void doAssign(AssginMenuVo assginMenuVo) {
        //删除已分配的权限
        sysRoleMenuService.remove(new QueryWrapper<SysRoleMenu>().eq("role_id", assginMenuVo.getRoleId()));
        //遍历所有已选择的权限id
        List<SysRoleMenu> saveRoleMenus = new ArrayList<>();
        for (String menuId : assginMenuVo.getMenuIdList()) {
            if (menuId != null) {
                //创建SysRoleMenu对象
                SysRoleMenu sysRoleMenu = new SysRoleMenu();
                sysRoleMenu.setMenuId(menuId);
                sysRoleMenu.setRoleId(assginMenuVo.getRoleId());
                //添加新权限
                saveRoleMenus.add(sysRoleMenu);
            }
        }
        if (saveRoleMenus.size() > 0) {
            sysRoleMenuService.saveBatch(saveRoleMenus);
        }
    }

    @Override
    public List<SysMenu> findUserMenuList(String userName) {
        SysUser sysUser = sysUserService.getByUsername(userName);
        String userId = sysUser.getId();
        //超级管理员admin账号id为：1
        List<SysMenu> sysMenuList = null;
        if ("1".equals(userId)) {
            QueryWrapper<SysMenu> queryWrapper = new QueryWrapper();
            queryWrapper.ne("type", 2);
            queryWrapper.orderByAsc("sort_value");
            sysMenuList = sysMenuMapper.selectList(queryWrapper);
        } else {
            List<Integer> typeList = new ArrayList<>();
            typeList.add(0);
            typeList.add(1);
            sysMenuList = sysMenuMapper.findListByUserId(userId, null, typeList);
        }
        //构建树形数据
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(sysMenuList);
        return sysMenuTreeList;
    }


    @Override
    public List<String> findUserPermsList(String userId) {
        //超级管理员admin账号id为：1
        List<SysMenu> sysMenuList = null;
        if ("1".equals(userId)) {
            sysMenuList = this.list();
        } else {
            sysMenuList = sysMenuMapper.findListByUserId(userId, 2, null);
        }
        //创建返回的集合
        List<String> permissionList = new ArrayList<>();
        for (SysMenu sysMenu : sysMenuList) {
            if (sysMenu.getType() == 2) {
                permissionList.add(sysMenu.getPerms());
            }
        }
        return permissionList;
    }

}

