package com.v3.system.controller;

import com.v3.common.result.Result;
import com.v3.model.system.SysMenu;
import com.v3.system.service.SysMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "菜单管理")
@RestController
@RequestMapping("/admin/system/sysMenu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @PreAuthorize("hasAuthority('bnt.sysMenu.list')")
    @ApiOperation(value = "获取所有菜单")
    @GetMapping("findNodes")
    public Result findNodes() {
        List<SysMenu> list = sysMenuService.findNodes();
        return Result.ok(list);
    }

    @PreAuthorize("hasAuthority('bnt.sysMenu.list')")
    @ApiOperation(value = "获取目录")
    @GetMapping("findDir/{notId}")
    public Result findDir(@PathVariable String notId) {
        List<SysMenu> list = sysMenuService.findDir(notId);
        return Result.ok(list);
    }

    @PreAuthorize("hasAuthority('bnt.sysMenu.list')")
    @ApiOperation(value = "获取菜单")
    @GetMapping("findMenu")
    public Result findMenu() {
        List<SysMenu> list = sysMenuService.findMenu();
        return Result.ok(list);
    }

    @PreAuthorize("hasAuthority('bnt.sysMenu.add')")
    @ApiOperation(value = "新增菜单")
    @PostMapping("save")
    public Result save(@RequestBody SysMenu permission) {
        sysMenuService.save(permission);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.sysMenu.list')")
    @ApiOperation(value = "获取菜单详情")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable String id) {
        SysMenu sysMenu = sysMenuService.getById(id);
        SysMenu parent = sysMenuService.getById(sysMenu.getParentId());
        if (parent != null) {
            sysMenu.setParentName(parent.getName());
        }
        return Result.ok(sysMenu);
    }

    @PreAuthorize("hasAuthority('bnt.sysMenu.update')")
    @ApiOperation(value = "修改菜单")
    @PostMapping("update")
    public Result updateById(@RequestBody SysMenu permission) {
        sysMenuService.updateById(permission);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.sysMenu.remove')")
    @ApiOperation(value = "删除菜单")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<String> idList) {
        boolean b = sysMenuService.removeByIds(idList);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @PreAuthorize("hasAuthority('bnt.sysMenu.list')")
    @ApiOperation(value = "根据角色获取菜单")
    @GetMapping("/toAssign/{roleId}")
    public Result toAssign(@PathVariable String roleId) {
        List<String> list = sysMenuService.findSysMenuByRoleId(roleId);
        return Result.ok(list);
    }

}
