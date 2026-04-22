package com.v3.business.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.v3.PanCHeck;
import com.v3.business.model.CommodityCommodityinfos;
import com.v3.business.model.CommodityDownloadLink;
import com.v3.business.model.CommodityTypes;
import com.v3.business.service.*;
import com.v3.business.vo.CommodityCommodityinfosQueryVo;
import com.v3.common.result.Result;
import com.v3.common.result.ResultCodeEnum;
import com.v3.common.utils.HeaderConstant;
import com.v3.common.Constants;
import com.v3.common.utils.OssProperties;
import com.v3.common.utils.OriginCheckUtil;
import com.v3.model.system.SysUser;
import com.v3.system.service.SysUserService;
import com.v3.system.utils.DeviceIdUtils;
import com.v3.system.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;*/
import java.util.concurrent.TimeUnit;

/**
 * @author administrator
* @微信 zzia6789
* @官网 https://mf5240.asia
 * @version 1.0
 * @description 素材信息表
 * @date 2025-04-06 23:41:35
 */
@Api(tags = "素材信息表")
@RestController
@RequestMapping("/business/commodityCommodityinfos")
public class CommodityCommodityinfosController {
    // 格式化游标时间用，确保传给数据库的是合法的 DATETIME 字符串
    private static final SimpleDateFormat CURSOR_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private CommodityCommodityinfosService commodityCommodityinfosService;

    @Autowired
    private CommodityDownloadLinkService commodityDownloadLinkService;

    @Autowired
    private SysUserService sysUserService;


    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private OriginCheckUtil originCheckUtil;

    @Autowired
    private CommodityTypesService commodityTypesService;


    @Autowired
    private OssProperties ossProperties;


    @PreAuthorize("hasAuthority('bnt.commodityCommodityinfos.list')")
    @ApiOperation(value = "获取分页列表")
    @GetMapping("/{page}/{limit}")
    public Result index(@ApiParam(name = "page", value = "当前页码", required = true)
                        @PathVariable Long page,
                        @ApiParam(name = "limit", value = "每页记录数", required = true)
                        @PathVariable Long limit,
                        @ApiParam(name = "commodityCommodityinfosQueryVo", value = "查询对象", required = false)
                        CommodityCommodityinfosQueryVo commodityCommodityinfosQueryVo, HttpServletRequest request) {
        Page<CommodityCommodityinfos> pageParam = new Page<>(page, limit);
        IPage<CommodityCommodityinfos> pageModel = commodityCommodityinfosService.selectPage(pageParam, commodityCommodityinfosQueryVo);
        Map<String, Object> data = new HashMap<>();
        data.put("vals", pageModel);
        data.put("ctxPath", ossProperties.getPrefix());
        return Result.ok(data);
    }

    @PreAuthorize("hasAuthority('bnt.commodityCommodityinfos.list')")
    @ApiOperation(value = "导出全部素材为Excel")
    @GetMapping("/export")
    public void export(CommodityCommodityinfosQueryVo commodityCommodityinfosQueryVo,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        // 查询全部数据（按当前查询条件）
        List<CommodityCommodityinfos> list = commodityCommodityinfosService.queryList(commodityCommodityinfosQueryVo);

        String ctxPath = ossProperties.getPrefix();

        StringBuilder sb = new StringBuilder();
        // 为了让 Excel 正确识别 UTF-8，在文件开头添加 BOM
        sb.append('\uFEFF');
        // 表头：名称, 链接(带前缀), 详情, 图片地址
        sb.append("名称,链接,详情,图片地址\n");

        for (CommodityCommodityinfos item : list) {
            String img = item.getImg();
            img = img.startsWith("http") ? img : ctxPath + img;

            sb.append(toCsvCell(item.getName())).append(',')
                    .append(toCsvCell(item.getTypes())).append(',')
                    .append(toCsvCell(item.getDetails())).append(',')
                    .append(toCsvCell(img))
                    .append('\n');
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        response.reset();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setContentLength(bytes.length);

        String fileName = URLEncoder.encode("素材信息导出", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition",
                "attachment;filename*=UTF-8''" + fileName + ".csv");

        try (ServletOutputStream out = response.getOutputStream()) {
            out.write(bytes);
            out.flush();
        } catch (ClientAbortException e) {
            // 客户端主动取消或中断连接，属于正常情况，这里忽略异常以避免大量错误日志
        }
    }

    /**
     * 简单的CSV单元格转义
     */
    private String toCsvCell(String value) {
        if (value == null) {
            return "";
        }
        String v = value.replace("\"", "\"\"")
                .replace("\r", " ")
                .replace("\n", " ");
        return "\"" + v + "\"";
    }


    /**
     * 前台首页最新素材 - 游标分页版本（效率更高）
     * 入参：limit, name, cursorCreateTime, cursorId
     * 返回：records, nextCursorCreateTime, nextCursorId, ctxPath
     */
    @ApiOperation(value = "前台首页最新素材-游标分页")
    @GetMapping("/lastListCursor")
    @CrossOrigin
    public Result lastListCursor(@ApiParam(name = "commodityCommodityinfosQueryVo", value = "查询对象", required = false)
                                 CommodityCommodityinfosQueryVo commodityCommodityinfosQueryVo,
                                 HttpServletRequest request) {
        // 检查请求来源
        if (!originCheckUtil.checkOrigin(request)) {
            return Result.fail("请求来源不合法");
        }

        Integer limit = commodityCommodityinfosQueryVo.getLimit() == null
                ? 12
                : commodityCommodityinfosQueryVo.getLimit().intValue();
        String name = commodityCommodityinfosQueryVo.getName();
        String cursorCreateTime = commodityCommodityinfosQueryVo.getCursorCreateTime();
        String cursorId = commodityCommodityinfosQueryVo.getCursorId();

        QueryWrapper<CommodityCommodityinfos> wrapper = new QueryWrapper<>();
        wrapper.select( "id","name", "img",  "details", "create_time");

        // 缓存 key：name + limit + cursorCreateTime + cursorId
        String cacheKey = String.format("lastListCursor:%s:%d:%s:%s",
                StringUtils.isBlank(name) ? "" : name,
                limit,
                StringUtils.isBlank(cursorCreateTime) ? "" : cursorCreateTime,
                StringUtils.isBlank(cursorId) ? "" : cursorId);

        // 【1. 先尝试从 Redis 获取缓存】
        Object cacheVal = redisTemplate.opsForValue().get(cacheKey);
        if (cacheVal instanceof String) {
            try {
                Map<String, Object> cacheData = JSON.parseObject((String) cacheVal, Map.class);
                System.out.println("从缓存获取的数据");
                return Result.ok(cacheData);
            } catch (Exception e) {
                // 缓存解析失败则忽略，继续走数据库
            }
        }

        // 【2. 添加分布式锁防止缓存击穿】
        String lockKey = cacheKey + ":lock";
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 10, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                // 双重检查
                cacheVal = redisTemplate.opsForValue().get(cacheKey);
                if (cacheVal instanceof String) {
                    try {
                        Map<String, Object> cacheData = JSON.parseObject((String) cacheVal, Map.class);
                        System.out.println("从缓存获取的数据");
                        return Result.ok(cacheData);
                    } catch (Exception e) {
                        // 忽略
                    }
                }

                // 使用全文索引查询（性能更好）
                if (StringUtils.isNotBlank(name)) {
                    wrapper.apply("MATCH(name) AGAINST({0} IN BOOLEAN MODE)", name);
                }

                // 游标条件：create_time < cursorTime OR (create_time = cursorTime AND id < cursorId)
                if (StringUtils.isNotBlank(cursorCreateTime)) {
                    wrapper.and(w ->
                            w.lt("create_time", cursorCreateTime)
                                    .or()
                                    .eq("create_time", cursorCreateTime).lt("id", cursorId)
                    );
                }

                wrapper.orderByDesc("create_time").orderByDesc("id");
                wrapper.last("LIMIT " + limit);

                List<CommodityCommodityinfos> records = commodityCommodityinfosService.list(wrapper);

                String nextCursorCreateTime = null;
                String nextCursorId = null;
                if (records != null && !records.isEmpty()) {
                    CommodityCommodityinfos last = records.get(records.size() - 1);
                    if (last.getCreateTime() != null) {
                        nextCursorCreateTime = CURSOR_TIME_FORMAT.format(last.getCreateTime());
                    }
                    nextCursorId = last.getId();
                }

                Map<String, Object> data = new HashMap<>();
                data.put("records", records);
                data.put("nextCursorCreateTime", nextCursorCreateTime);
                data.put("nextCursorId", nextCursorId);
                data.put("ctxPath", ossProperties.getPrefix());

                Result ok = Result.ok(data);

                // 【3. 存入缓存 + 防雪崩策略：基础时间 + 随机偏移】
                int baseMinutes = 30;
                int randomOffset = (int)(Math.random() * 10); // 0-10 分钟随机
                int expireTime = baseMinutes + randomOffset;
                redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(data), expireTime, TimeUnit.MINUTES);
                return ok;

            } finally {
                // 释放锁
                redisTemplate.delete(lockKey);
            }
        } else {
            // 获取锁失败 → 降级方案：等待后查库
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 再次尝试读缓存（可能已被其他线程写入）
            cacheVal = redisTemplate.opsForValue().get(cacheKey);
            if (cacheVal instanceof String) {
                try {
                    Map<String, Object> cacheData = JSON.parseObject((String) cacheVal, Map.class);
                    System.out.println("从缓存获取的数据（降级路径）");
                    return Result.ok(cacheData);
                } catch (Exception e) {
                    // 忽略
                }
            }

            // 最终降级：直接查数据库
            if (StringUtils.isNotBlank(name)) {
                wrapper.apply("MATCH(name) AGAINST({0} IN BOOLEAN MODE)", name);
            }

            if (StringUtils.isNotBlank(cursorCreateTime)) {
                wrapper.and(w ->
                        w.lt("create_time", cursorCreateTime)
                                .or()
                                .eq("create_time", cursorCreateTime).lt("id", cursorId)
                );
            }

            wrapper.orderByDesc("create_time").orderByDesc("id");
            wrapper.last("LIMIT " + limit);

            List<CommodityCommodityinfos> records = commodityCommodityinfosService.list(wrapper);

            String nextCursorCreateTime = null;
            String nextCursorId = null;
            if (records != null && !records.isEmpty()) {
                CommodityCommodityinfos last = records.get(records.size() - 1);
                if (last.getCreateTime() != null) {
                    nextCursorCreateTime = CURSOR_TIME_FORMAT.format(last.getCreateTime());
                }
                nextCursorId = last.getId();
            }

            Map<String, Object> data = new HashMap<>();
            data.put("records", records);
            data.put("nextCursorCreateTime", nextCursorCreateTime);
            data.put("nextCursorId", nextCursorId);
            data.put("ctxPath", ossProperties.getPrefix());

            return Result.ok(data);
        }
    }

    @ApiOperation(value = "前台首页最新素材总数统计")
    @GetMapping("/lastListCount")
    @CrossOrigin
    public Result lastListCount(@ApiParam(name = "commodityCommodityinfosQueryVo", value = "查询对象", required = false)
                                CommodityCommodityinfosQueryVo commodityCommodityinfosQueryVo,
                                HttpServletRequest request) {
        // 检查请求来源
        if (!originCheckUtil.checkOrigin(request)) {
            return Result.fail("请求来源不合法");
        }

        String name = commodityCommodityinfosQueryVo.getName();
        String cacheKey = String.format("lastListCount:%s",
                name!=null?name:"");
        Object cacheVal = redisTemplate.opsForValue().get(cacheKey);
        if (cacheVal instanceof String) {
            try {
                Map<String, Object> cacheData = JSON.parseObject((String) cacheVal, Map.class);
                System.out.println("从缓存获取的数据count");
                return Result.ok(cacheData);
            } catch (Exception e) {
                // 缓存解析失败则忽略，继续走数据库
            }
        }

        // 添加分布式锁防止缓存击穿
        String lockKey = cacheKey + ":lock";
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 10, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                // 双重检查，防止重复计算
                cacheVal = redisTemplate.opsForValue().get(cacheKey);
                if (cacheVal instanceof String) {
                    try {
                        Map<String, Object> cacheData = JSON.parseObject((String) cacheVal, Map.class);
                        System.out.println("从缓存获取的数据count");
                        return Result.ok(cacheData);
                    } catch (Exception e) {
                        // 忽略解析错误
                    }
                }

                QueryWrapper<CommodityCommodityinfos> wrapper = new QueryWrapper<>();
                if (StringUtils.isNotBlank(name)) {
                    wrapper.apply("MATCH(name) AGAINST({0} IN BOOLEAN MODE)", name);
                }

                long total = commodityCommodityinfosService.count(wrapper);
                Map<String, Object> data = new HashMap<>();
                data.put("total", total);

                // 添加随机过期时间防止缓存雪崩（30分钟基础时间 + 0-10分钟随机偏移）
                int baseMinutes = 30;
                int randomOffset = (int)(Math.random() * 10);
                int expireTime = baseMinutes + randomOffset;
                redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(data), expireTime, TimeUnit.MINUTES);
                return Result.ok(data);
            } finally {
                // 释放锁
                redisTemplate.delete(lockKey);
            }
        } else {
            // 获取锁失败，可能是其他线程正在计算，等待一下后直接查数据库
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // 直接查询数据库，不使用缓存
            QueryWrapper<CommodityCommodityinfos> wrapper = new QueryWrapper<>();
            if (StringUtils.isNotBlank(name)) {
                wrapper.apply("MATCH(name) AGAINST({0} IN BOOLEAN MODE)", name);
            }

            long total = commodityCommodityinfosService.count(wrapper);
            Map<String, Object> data = new HashMap<>();
            data.put("total", total);
            return Result.ok(data);
        }
    }


    // 命中缓存也要增加浏览量：直接按 id 自增，避免先查再改
        /*        try {
                    if(!"true".equals(isGetLink)) {// 非获取链接才增加浏览量
                        UpdateWrapper<CommodityCommodityinfos> uw = new UpdateWrapper<>();
                        uw.eq("id", id).setSql("views = IFNULL(views, 0) + 1");
                        commodityCommodityinfosService.update(uw);
                    }else{// 获取链接才增加下载量
                        UpdateWrapper<CommodityCommodityinfos> uw = new UpdateWrapper<>();
                        uw.eq("id", id).setSql("downs = IFNULL(downs, 0) + 1");
                        commodityCommodityinfosService.update(uw);
                    }
                } catch (Exception ignore) {
                }*/
    @PreAuthorize("hasAuthority('bnt.commodityCommodityinfos.list')")
    @ApiOperation(value = "获取素材信息表")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable String id, HttpServletRequest request) {
        CommodityCommodityinfos commodityCommodityinfos = commodityCommodityinfosService.getById(id);
        // 【4. 处理业务逻辑】
        commodityCommodityinfos.setUsername("admin");
        if (StringUtils.isNotBlank(commodityCommodityinfos.getImg())) {
            commodityCommodityinfos.setImg(ossProperties.getPrefix()+ commodityCommodityinfos.getImg());
        }
        if (StringUtils.isNotBlank(commodityCommodityinfos.getLbImg())) {
            commodityCommodityinfos.setLbImg(ossProperties.getPrefix() + commodityCommodityinfos.getLbImg());
        }
        // 将 size 从 KB 转换为 MB
        if (commodityCommodityinfos.getSize() > 0) {
            long sizeInMB = commodityCommodityinfos.getSize() / (1024 * 1024);
            commodityCommodityinfos.setSize(sizeInMB);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("commodityInfo", commodityCommodityinfos);
        List<CommodityDownloadLink> downloadLinks = commodityDownloadLinkService.getByCommodityId(id);
         result.put("downloadLinks", downloadLinks);
        Result ok = Result.ok(result);
        return ok;
    }


    /**
     * 获取素材详情 - 带互斥锁版本（防击穿）
     * 适用于热点素材被高频访问的场景
     */
    @ApiOperation(value = "获取素材信息表 - 增强版（防击穿）")
    @GetMapping("/getWithLock/{id}")
    @CrossOrigin
    public Result getWithLock(@PathVariable String id, HttpServletRequest request) {
        // 检查请求来源
        if (!originCheckUtil.checkOrigin(request)) {
            return Result.fail("请求来源不合法");
        }

        String cacheKey = "commodity:get:" + id;
        String lockKey = "commodity:lock:" + id;

        // 1. 先尝试从缓存获取
        Object cacheVal = redisTemplate.opsForValue().get(cacheKey);
        if (cacheVal instanceof String) {
            try {
                Map<String, Object> cacheData = JSON.parseObject((String) cacheVal, Map.class);
                if ("NULL_OBJECT".equals(cacheData.get("__mark__"))) {
                    return Result.build(null, ResultCodeEnum.FAIL.getCode(), "素材不存在");
                }
                return Result.ok(cacheData);
            } catch (Exception e) {
                // 忽略解析错误
            }
        }

        // 2. 缓存未命中，尝试获取分布式锁
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 10, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                // 3. 获取锁成功，再次检查缓存（双重检查）
                cacheVal = redisTemplate.opsForValue().get(cacheKey);
                if (cacheVal instanceof String) {
                    try {
                        Map<String, Object> cacheData = JSON.parseObject((String) cacheVal, Map.class);
                        if ("NULL_OBJECT".equals(cacheData.get("__mark__"))) {
                            return Result.build(null, ResultCodeEnum.FAIL.getCode(), "素材不存在");
                        }
                        return Result.ok(cacheData);
                    } catch (Exception e) {
                        // 忽略
                    }
                }

                // 4. 执行数据库查询
                CommodityCommodityinfos commodityCommodityinfos = commodityCommodityinfosService.getById(id);

                // 5. 数据库也没有，缓存空对象
                if (commodityCommodityinfos == null) {
                    Map<String, Object> nullData = new HashMap<>();
                    nullData.put("__mark__", "NULL_OBJECT");
                    int randomSeconds = (int)(Math.random() * 300);
                    redisTemplate.opsForValue().set(cacheKey, nullData, 5 + randomSeconds / 60, TimeUnit.MINUTES);
                    return Result.build(null, ResultCodeEnum.FAIL.getCode(), "素材不存在");
                }

                // 6. 处理业务逻辑
                commodityCommodityinfos.setUsername("admin");
                if (StringUtils.isNotBlank(commodityCommodityinfos.getImg())) {
                    commodityCommodityinfos.setImg(ossProperties.getPrefix() + commodityCommodityinfos.getImg());
                }
                if (StringUtils.isNotBlank(commodityCommodityinfos.getLbImg())) {
                    commodityCommodityinfos.setLbImg(ossProperties.getPrefix() + commodityCommodityinfos.getLbImg());
                }
                if (commodityCommodityinfos.getSize() > 0) {
                    long sizeInMB = commodityCommodityinfos.getSize() / (1024 * 1024);
                    commodityCommodityinfos.setSize(sizeInMB);
                }

                Map<String, Object> result = new HashMap<>();
                result.put("commodityInfo", commodityCommodityinfos);

                //如果已经等于1是自己的夸克网盘链接直接分享出来， 9999代表是百度网盘链接(暂时没法处理，暂时只能直接分享出来)
                if("1".equals(commodityCommodityinfos.getIsUpdate()) || commodityCommodityinfos.getLikes() == 9999) {
                    List<CommodityDownloadLink> downloadLinks = commodityDownloadLinkService.getByCommodityId(id);
                    result.put("downloadLinks", downloadLinks);
                }else{
                    //把自己的分享链接分享出来
                    CommodityTypes commodityTypes = commodityTypesService.getById("2036519036380106754");
                    result.put("shareLink", commodityTypes.getSeconds()+"#"+id);
                }

                // 7. 存入缓存 + 随机过期时间
                Result ok = Result.ok(result);
                int baseMinutes = 30;
                int randomOffset = (int)(Math.random() * 10);
                redisTemplate.opsForValue().set(cacheKey, result, baseMinutes + randomOffset, TimeUnit.MINUTES);
                return ok;

            } finally {
                // 8. 释放锁
                try {
                    redisTemplate.delete(lockKey);
                } catch (Exception ignore) {
                }
            }
        } else {
            // 9. 获取锁失败，等待重试或直接返回（降级方案）
            try {
                Thread.sleep(50); // 等待 50ms 后重试
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // 递归调用一次（最多重试一次）
            return getWithLock(id, request);
        }
    }

    @ApiOperation(value = "根据id获取下载链接")
    @PostMapping("/getTypes")
    public Result getTypesById(@RequestParam String text, HttpServletRequest request) {
        if (!originCheckUtil.checkOrigin(request)) {
            return Result.fail("请求来源不合法");
        }

        if(org.springframework.util.StringUtils.isEmpty(text)) {
            return Result.fail("口令不能为空");
        }

        String ids [] = text.split("#");

        if(ids.length < 2){
            return Result.fail("口令格式错误");
        }

        String code = ids [0];
        CommodityTypes commodityTypes = commodityTypesService.getByCodeCache();
        if(commodityTypes!=null){
            if(!code.equals(commodityTypes.getSeconds())){
                return Result.fail("口令错误");
            }
        }

        DeviceIdUtils.generateAndSaveDeviceId(request, ids[1],"typesLog");

        String id = ids [1];

        String cacheKey = "commodity:types:" + id;
        String lockKey = "commodity:types:lock:" + id;

        Object cacheVal = redisTemplate.opsForValue().get(cacheKey);
        if (cacheVal != null) {
            return Result.ok(cacheVal);
        }

        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 10, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                cacheVal = redisTemplate.opsForValue().get(cacheKey);
                if (cacheVal != null) {
                    return Result.ok(cacheVal);
                }

                CommodityCommodityinfos commodityInfo = commodityCommodityinfosService.getById(id);
                String name = commodityInfo != null ? commodityInfo.getName() : null;
                List<CommodityDownloadLink> downloadLinks = commodityDownloadLinkService.getByCommodityId(id);

                int baseMinutes = 30;
                int randomOffset = (int)(Math.random() * 10);
                int expireTime = baseMinutes + randomOffset;

                Map<String, Object> resultData = new HashMap<>();
                resultData.put("name", name);
                resultData.put("list", downloadLinks != null ? downloadLinks : new ArrayList<>());

                if (downloadLinks == null || downloadLinks.isEmpty()) {
                    redisTemplate.opsForValue().set(cacheKey, resultData, 5, TimeUnit.MINUTES);
                    return Result.ok(resultData);
                }

                redisTemplate.opsForValue().set(cacheKey, resultData, expireTime, TimeUnit.MINUTES);
                return Result.ok(resultData);

            } finally {
                try {
                    redisTemplate.delete(lockKey);
                } catch (Exception ignore) {
                }
            }
        } else {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            cacheVal = redisTemplate.opsForValue().get(cacheKey);
            if (cacheVal != null) {
                return Result.ok(cacheVal);
            }

            CommodityCommodityinfos commodityInfo = commodityCommodityinfosService.getById(id);
            String name = commodityInfo != null ? commodityInfo.getName() : null;
            List<CommodityDownloadLink> downloadLinks = commodityDownloadLinkService.getByCommodityId(id);

            Map<String, Object> resultData = new HashMap<>();
            resultData.put("name", name);
            resultData.put("list", downloadLinks != null ? downloadLinks : new ArrayList<>());
            return Result.ok(resultData);
        }
    }

    @PreAuthorize("hasAuthority('bnt.commodityCommodityinfos.remove')")
    @ApiOperation(value = "清除所有Redis缓存")
    @DeleteMapping("/clearAllCache")
    public Result clearAllCache() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            return Result.ok("缓存清除成功");
        } catch (Exception e) {
            return Result.fail("缓存清除失败：" + e.getMessage());
        }
    }






    @PreAuthorize("hasAuthority('bnt.commodityCommodityinfos.list')")
    @ApiOperation(value = "根据素材ID获取下载地址列表")
    @GetMapping("/downloadLinks/{commodityId}")
    public Result getDownloadLinks(@PathVariable String commodityId) {
        List<CommodityDownloadLink> downloadLinks = commodityDownloadLinkService.getByCommodityId(commodityId);
        return Result.ok(downloadLinks);
    }


    @PreAuthorize("hasAuthority('bnt.commodityCommodityinfos.add')")
    @ApiOperation(value = "保存素材信息表")
    @PostMapping("/save")
    public Result save(CommodityCommodityinfos commodityCommodityinfos,
                       @RequestParam(required = false) String downloadLinks) {
        SysUser sysUser = sysUserService.getById(UserUtil.getUserId());
        commodityCommodityinfos.setUserId(sysUser.getId());
        commodityCommodityinfos.setUsername(sysUser.getName());
        if (StringUtils.isBlank(commodityCommodityinfos.getIsRecom())) {
            commodityCommodityinfos.setIsRecom("否");
        }
        boolean saved = commodityCommodityinfosService.save(commodityCommodityinfos);
        String firstUrl = null;
        // 保存下载地址
        if (saved && StringUtils.isNotBlank(downloadLinks)) {
            try {
                JSONArray linksArray = JSON.parseArray(downloadLinks);
                for (int i = 0; i < linksArray.size(); i++) {
                    JSONObject linkObj = linksArray.getJSONObject(i);
                    String name = linkObj.getString("name");
                    String url = linkObj.getString("url");
                    if(i==0){
                        firstUrl = url;
                        commodityCommodityinfos.setTypes(firstUrl);
                        commodityCommodityinfos.setFile(null);  // 清除 file，避免重复上传
                        commodityCommodityinfosService.updateById(commodityCommodityinfos);
                    }
                    // 只保存名称和链接都不为空的记录
                    if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(url)) {
                        CommodityDownloadLink downloadLink = new CommodityDownloadLink();
                        downloadLink.setCommodityId(commodityCommodityinfos.getId());
                        downloadLink.setName(name);
                        downloadLink.setUrl(url);
                        commodityDownloadLinkService.save(downloadLink);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 下载地址保存失败不影响主流程
            }
        }

        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.commodityCommodityinfos.add')")
    @ApiOperation(value = "批量保存素材信息表")
    @PostMapping("/batchSave")
    public Result batchSave(@RequestParam(required = false) MultipartFile[] files,
                            @RequestParam(required = false) String types,
                            @RequestParam(required = false) String details,
                            @RequestParam(required = false) String isPub,
                            @RequestParam(required = false) String isRecom) {
        List<CommodityCommodityinfos> savedList = new ArrayList<>();

        if (files == null || files.length == 0) {
            return Result.build(null, ResultCodeEnum.FAIL.getCode(), "请选择至少一个文件");
        }

        for (MultipartFile file : files) {
            CommodityCommodityinfos commodityCommodityinfos = new CommodityCommodityinfos();
            //name 不包含后缀
            commodityCommodityinfos.setName(file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf(".")));
            commodityCommodityinfos.setTypes(types);
            commodityCommodityinfos.setDetails(details);
            commodityCommodityinfos.setIsPub(isPub);
            commodityCommodityinfos.setIsRecom(isRecom);
            commodityCommodityinfos.setUserId(UserUtil.getUserId());
            commodityCommodityinfos.setFile(file);

            if (StringUtils.isBlank(commodityCommodityinfos.getIsRecom())) {
                commodityCommodityinfos.setIsRecom("否");
            }

            boolean saved = commodityCommodityinfosService.save(commodityCommodityinfos);
            if (saved) {
                savedList.add(commodityCommodityinfos);
            }
        }
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.commodityCommodityinfos.update')")
    @ApiOperation(value = "更新素材信息表")
    @PutMapping("/update")
    public Result updateById(CommodityCommodityinfos commodityCommodityinfos,
                           @RequestParam(required = false) String downloadLinks) {
       /* commodityCommodityinfos.setIsUpdate("1");*/
        // 更新下载链接
        if (StringUtils.isNotBlank(downloadLinks)) {
            try {
                // 先删除原有的下载链接
                commodityDownloadLinkService.deleteByCommodityId(commodityCommodityinfos.getId());

                // 保存新的下载链接
                JSONArray linksArray = JSON.parseArray(downloadLinks);
                for (int i = 0; i < linksArray.size(); i++) {
                    JSONObject linkObj = linksArray.getJSONObject(i);
                    String name = linkObj.getString("name");
                    String url = linkObj.getString("url");
                    String id = linkObj.getString("id");

                    // 只保存名称和链接都不为空的记录
                    if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(url)) {
                        CommodityDownloadLink downloadLink = new CommodityDownloadLink();
                       /* if (StringUtils.isNotBlank(id)) {
                            downloadLink.setId(id); // 如果有ID说明是更新现有记录
                        }*/
                        downloadLink.setCommodityId(commodityCommodityinfos.getId());
                        downloadLink.setName(name);
                        downloadLink.setUrl(url);
                        commodityDownloadLinkService.save(downloadLink);
                        if(i==0){
                            commodityCommodityinfos.setTypes(url);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 下载地址保存失败不影响主流程
            }
        }
        commodityCommodityinfosService.updateById(commodityCommodityinfos);

        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.commodityCommodityinfos.remove')")
    @ApiOperation(value = "删除素材信息表")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable String id) {
        commodityCommodityinfosService.removeById(id);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.commodityCommodityinfos.remove')")
    @ApiOperation(value = "根据id列表删除")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<String> idList) {
        boolean b = commodityCommodityinfosService.removeByIds(idList);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }


     /*   @ApiOperation(value = "chekcPan")
    @CrossOrigin
    @GetMapping("/chekcPan")
    public Result chekcPan() {

        new Thread(
                () -> {
                    List list = commodityCommodityinfosService.list();
                    for (int i = 0; i < list.size(); i++) {
                        CommodityCommodityinfos commodityCommodityinfos = (CommodityCommodityinfos) list.get(i);
                        String pan = commodityCommodityinfos.getTypes();
                        if (StringUtils.isNotBlank(pan) && pan.contains("quark")) {
                             boolean link = PanCHeck.isQuarkLinkSaveable(pan);
                             if(!link){
                                 commodityCommodityinfos.setLikes(999);
                                 commodityCommodityinfosService.updateById(commodityCommodityinfos);
                             }
                        }
                    }
                }
        ).start();

        return  Result.ok();
    }*/


   /* @ApiOperation(value = "清理无用图片文件")
    @GetMapping("/cleanUnusedImages")
    @CrossOrigin
    public Result cleanUnusedImages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String imgDir = "D:/project/resource/movie/img/photos";
                Map<String, Object> result = new HashMap<>();
                List<String> deletedFiles = new ArrayList<>();
                List<String> errorFiles = new ArrayList<>();

                try {
                    Path dirPath = Paths.get(imgDir);

                    if (!Files.exists(dirPath)) {
                        System.out.println("目录不存在");
                    }

                    int totalFiles = 0;
                    int deletedCount = 0;

                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
                        for (Path file : stream) {
                            if (Files.isRegularFile(file)) {
                                String fileName = file.getFileName().toString();

                                totalFiles++;

                                // 构建查询条件：/img/photos/文件名
                                String imgPath = "/img/photos/" + fileName;

                                // 查询数据库中是否存在该图片路径
                                QueryWrapper queryWrapper = new QueryWrapper();
                                queryWrapper.eq("img",imgPath);

                                List<CommodityCommodityinfos> list = commodityCommodityinfosService.list(queryWrapper);

                                if (list == null || list.isEmpty()) {
                                    // 数据库中没有找到，删除文件
                                    try {
                                        Files.delete(file);
                                        deletedFiles.add(fileName);
                                        deletedCount++;
                                        System.out.println("已删除无用图片：" + fileName);
                                    } catch (Exception e) {
                                        errorFiles.add(fileName + " - 删除失败：" + e.getMessage());
                                        System.err.println("删除文件失败：" + fileName + ", 错误：" + e.getMessage());
                                    }
                                }else{
                                    System.out.println("找到了数据="+fileName);
                                }
                            }
                        }
                    }

                    result.put("totalFiles", totalFiles);
                    result.put("deletedCount", deletedCount);
                    result.put("remainingCount", totalFiles - deletedCount);
                    result.put("deletedFiles", deletedFiles);
                    result.put("errorFiles", errorFiles);

                    System.out.println("totalFiles="+totalFiles);
                    System.out.println("deletedCount="+deletedCount);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return Result.ok();
    }*/

    /*@ApiOperation(value = "同步数据方法")
    @GetMapping("/startDo")
    @CrossOrigin
    public Result startDo() {

        new Thread(() -> {
            String filePath = "D:\\project\\resource\\movie\\result.json"; // 你可以改为从命令行参数读取

            try {
                String content = new String(Files.readAllBytes(Paths.get(filePath)));
                org.json.JSONObject root = new org.json.JSONObject(content);
                org.json.JSONArray messages = root.getJSONArray("messages");
                for (int i = 0; i < messages.length(); i++) {
                    org.json.JSONObject msg = messages.getJSONObject(i);

                    // 只处理 type = "message"
                    if (!"message".equals(msg.optString("type"))) {
                        continue;
                    }
                    System.out.println("=== 记录 " + (i + 1) + " ===");
                    String photo = null;
                    // 读取 photo 字段
                    if (msg.has("photo")) {
                        photo = msg.getString("photo");
                        System.out.println("photo: " + photo);
                    } else {
                        System.out.println("photo: 无");
                    }
                    // 读取 text_entities 数组
                    org.json.JSONArray textEntities = msg.optJSONArray("text_entities");
                    if (textEntities != null && !textEntities.isEmpty()) {
                        StringBuilder plainText = new StringBuilder();
                        String link = null;
                        String firstText = null;
                        for (int j = 0; j < textEntities.length(); j++) {
                            org.json.JSONObject entity = textEntities.getJSONObject(j);
                            String type = entity.optString("type");
                            String text = entity.optString("text");

                            if ("plain".equals(type)) {
                                plainText.append(text);
                                if (firstText == null) {
                                    firstText = text;
                                }
                            } else if ("link".equals(type)) {
                                link = text;
                            }
                        }

                        if (plainText.length() > 0) {
                            System.out.println("plain text: " + plainText.toString());
                        }

                        if (link != null) {
                            System.out.println("link: " + link);
                        }

                        CommodityCommodityinfos commodityCommodityinfos = new CommodityCommodityinfos();
                        commodityCommodityinfos.setName(firstText);
                        commodityCommodityinfos.setDetails(plainText.toString());
                        commodityCommodityinfos.setLbImg(photo);
                        commodityCommodityinfos.setImg(photo);
                        //为了区分是新同步进来的数据
                        commodityCommodityinfos.setLikes(888);
                        commodityCommodityinfosService.getBaseMapper().insert(commodityCommodityinfos);

                        CommodityDownloadLink commodityDownloadLink = new CommodityDownloadLink();
                        commodityDownloadLink.setCommodityId(commodityCommodityinfos.getId());
                        commodityDownloadLink.setName("夸克");
                        commodityDownloadLink.setUrl(link);
                        commodityDownloadLinkService.save(commodityDownloadLink);

                    } else {
                        System.out.println("text_entities: 无内容");
                    }

                    System.out.println();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return null;

    }*/

  /*  @ApiOperation(value = "解析 Excel 文件并导入数据")
    @PostMapping("/parseExcel")
    @CrossOrigin
    public Result parseExcel() {
        String filePath = "C:\\Users\\Administrator\\Desktop\\sdata.xls";
        File file = new File(filePath);

        if (!file.exists()) {
            return Result.fail("文件不存在: " + filePath);
        }

        int successCount = 0;
        int failCount = 0;

        try (InputStream fis = Files.newInputStream(file.toPath());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();

            for (int i = 0; i < rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // 读取第一列：名称
                    Cell nameCell = row.getCell(0);
                    if (nameCell == null) continue;

                    String name = nameCell.getStringCellValue().trim();
                    if (name.isEmpty()) continue;

                    // 读取第二列：链接
                    Cell linkCell = row.getCell(1);
                    if (linkCell == null) continue;

                    String linkContent = linkCell.getStringCellValue().trim();
                    if (linkContent.isEmpty()) continue;

                    // 保存到 CommodityCommodityinfos
                    CommodityCommodityinfos commodity = new CommodityCommodityinfos();
                    commodity.setName(name);
                    commodity.setLikes(1111);
                    commodity.setIsUpdate("1");

                    boolean saveResult = commodityCommodityinfosService.save(commodity);
                    if (!saveResult) {
                        failCount++;
                        continue;
                    }

                    // 解析第二列的多个链接
                    // 使用正则表达式提取所有 http 开头的链接
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(http[s]?:\\/\\/[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?)");
                    java.util.regex.Matcher matcher = pattern.matcher(linkContent);

                    int linkIndex = 1;
                    int lastEnd = 0;

                    while (matcher.find()) {
                        String url = matcher.group(0);
                        int start = matcher.start();
                        int end = matcher.end();

                        // 提取链接名称（链接前面的文字）
                        String linkName = "";
                        if (start > lastEnd) {
                            linkName = linkContent.substring(lastEnd, start).trim();
                        }

                        // 如果没有名称，使用默认名称
                        if (linkName.isEmpty()) {
                            linkName = "下载链接 " + linkIndex;
                        }

                        // 保存到 CommodityDownloadLink
                        CommodityDownloadLink downloadLink = new CommodityDownloadLink();
                        downloadLink.setCommodityId(commodity.getId());
                        downloadLink.setName(linkName);
                        downloadLink.setUrl(url);
                        downloadLink.setCreateTime(new java.util.Date());
                        downloadLink.setUpdateTime(new java.util.Date());

                        commodityDownloadLinkService.save(downloadLink);

                        lastEnd = end;
                        linkIndex++;
                    }

                    successCount++;

                } catch (Exception e) {
                    e.printStackTrace();
                    failCount++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("解析 Excel 文件失败: " + e.getMessage());
        }

        return Result.ok("Excel 解析完成，成功: " + successCount + " 条，失败: " + failCount + " 条");
    }*/
}

