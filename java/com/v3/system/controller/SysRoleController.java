package com.v3.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.v3.common.result.Result;
import com.v3.model.system.SysRole;
import com.v3.model.vo.AssginMenuVo;
import com.v3.model.vo.SysRoleQueryVo;
import com.v3.system.exception.LanfException;
import com.v3.system.service.SysMenuService;
import com.v3.system.service.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "角色管理")
@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {
    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysMenuService sysMenuService;

    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation(value = "获取全部角色列表")
    @GetMapping("/findAll")
    public Result findAll() {
        List<SysRole> roleList = sysRoleService.list();
        return Result.ok(roleList);
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation(value = "条件分页查询")
    @GetMapping("/{page}/{limit}")
    public Result findPageQueryRole(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit,

            @ApiParam(name = "roleQueryVo", value = "查询对象", required = false)
            SysRoleQueryVo roleQueryVo) {
        Page<SysRole> pageParam = new Page<>(page, limit);
        IPage<SysRole> pageModel = sysRoleService.selectPage(pageParam, roleQueryVo);
        return Result.ok(pageModel);
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation(value = "获取角色")
    @GetMapping("/findRoleById/{id}")
    public Result get(@PathVariable String id) {
        SysRole role = sysRoleService.getById(id);
        return Result.ok(role);
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.add')")
    @ApiOperation(value = "新增角色")
    @PostMapping("/save")
    public Result save(@RequestBody SysRole role) {
        boolean b = sysRoleService.save(role);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @ApiOperation(value = "修改角色")
    @PostMapping("/update")
    public Result updateById(@RequestBody SysRole role) {
        boolean b = sysRoleService.updateById(role);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation(value = "删除角色")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable String id) {
        if ("2".equals(id) || "1793660824038866945".equals(id)) {
            throw new LanfException(9001, "默认角色不能删除");
        }
        boolean b = sysRoleService.removeById(id);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation(value = "根据id列表删除")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<String> idList) {
        if (idList.contains("2") || idList.contains("1793660824038866945")) {
            throw new LanfException(9001, "默认角色不能删除");
        }
        boolean b = sysRoleService.removeByIds(idList);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.assignAuth')")
    @ApiOperation(value = "给角色分配权限")
    @PostMapping("/doAuth")
    public Result doAuth(@RequestBody AssginMenuVo assginMenuVo) {
        sysMenuService.doAssign(assginMenuVo);
        return Result.ok();
    }
}
