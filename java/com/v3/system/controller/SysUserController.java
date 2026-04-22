package com.v3.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.v3.common.result.Result;
import com.v3.common.result.ResultCodeEnum;
import com.v3.model.system.SysUser;
import com.v3.model.vo.SysUserQueryVo;
import com.v3.system.exception.LanfException;
import com.v3.system.service.SysUserRoleService;
import com.v3.system.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/admin/system/sysUser")
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysUserRoleService sysUserRoleService;

    @PreAuthorize("hasAuthority('bnt.sysUser.list')")
    @ApiOperation(value = "获取分页列表")
    @GetMapping("/{page}/{limit}")
    public Result index(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit,
            @ApiParam(name = "userQueryVo", value = "查询对象", required = false)
            SysUserQueryVo userQueryVo, HttpServletRequest request) {
        Page<SysUser> pageParam = new Page<>(page, limit);
        IPage<SysUser> pageModel = sysUserService.selectPage(pageParam, userQueryVo);
        return Result.ok(pageModel);
    }

    @PreAuthorize("hasAuthority('bnt.sysUser.list')")
    @ApiOperation(value = "查询列表")
    @GetMapping("/list")
    public Result list(@ApiParam(name = "sysUserQueryVo", value = "查询对象", required = false)
                       SysUserQueryVo sysUserQueryVo) {
        List<SysUser> list = sysUserService.list();
        return Result.ok(list);
    }

    @PreAuthorize("hasAuthority('bnt.sysUser.list')")
    @ApiOperation(value = "获取用户")
    @GetMapping("/getUser/{id}")
    public Result get(@PathVariable String id) {
        SysUser user = sysUserService.getById(id);
        return Result.ok(user);
    }


    @PreAuthorize("hasAuthority('bnt.sysUser.add')")
    @ApiOperation(value = "保存用户")
    @PostMapping("/save")
    public Result save(@RequestBody SysUser user) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", user.getUsername());
        SysUser ex = sysUserService.getOne(queryWrapper);
        if (ex != null) {
            return Result.build(null, ResultCodeEnum.ACCOUNT_EXISTS);
        }
        sysUserService.save(user);
        return Result.ok();
    }

    @ApiOperation(value = "注册用户")
    @PostMapping("/register")
    @CrossOrigin
    public Result register(@RequestBody SysUser user) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", user.getUsername());
        SysUser ex = sysUserService.getOne(queryWrapper);
        if (ex != null) {
            return Result.build(null, ResultCodeEnum.ACCOUNT_EXISTS);
        } else {
            sysUserService.register(user);
            return Result.ok();
        }
    }

    @PreAuthorize("hasAuthority('bnt.sysUser.update')")
    @ApiOperation(value = "更新用户")
    @PostMapping("/update")
    public Result updateById(@RequestBody SysUser user, HttpServletRequest request) {
        if ("1".equals(user.getId())) {
            List<String> roleList = user.getRoleList();
            if (roleList!=null && !roleList.contains("2")) {
                throw new LanfException(9001, "默认管理员用户不能更改角色");
            }
        }
        sysUserService.updateById(user, request);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.sysUser.updateByUser')")
    @ApiOperation(value = "前台更新用户")
    @PostMapping("/updateByUser")
    public Result updateByUser(SysUser user, HttpServletRequest request) {
        sysUserService.updateById(user, request);
        return Result.ok(sysUserService.getById(user.getId()));
    }

    @PreAuthorize("hasAuthority('bnt.sysUser.update')")
    @ApiOperation(value = "更新状态")
    @GetMapping("updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable String id, @PathVariable Integer status) {
        sysUserService.updateStatus(id, status);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.sysUser.remove')")
    @ApiOperation(value = "根据id列表删除")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<String> idList) {
        if (idList.contains("1")) {
            throw new LanfException(9001, "默认管理员用户不能删除");
        }
        boolean b = sysUserService.removeByIds(idList);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

}
