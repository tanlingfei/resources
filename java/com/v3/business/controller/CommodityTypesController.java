package com.v3.business.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.v3.business.model.CommodityTypes;
import com.v3.business.service.CommodityTypesService;
import com.v3.business.vo.CommodityTypesQueryVo;
import com.v3.common.Constants;
import com.v3.common.result.Result;
import com.v3.common.utils.OssProperties;
import com.v3.common.utils.OssUtil;
import com.v3.common.utils.OriginCheckUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author administrator
* @微信 zzia6789
* @官网 https://mf5240.asia
 * @version 1.0
 * @description 素材分类描述
 * @date 2025-04-06 23:05:25
 */
@Api(tags = "素材分类描述")
@RestController
@RequestMapping("/business/commodityTypes")
public class CommodityTypesController {
    @Autowired
    private CommodityTypesService commodityTypesService;


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OssUtil ossUtil;

    @Autowired
    private OriginCheckUtil originCheckUtil;

    @Autowired
    private OssProperties ossProperties;


    @PreAuthorize("hasAuthority('bnt.commodityTypes.list')")
    @ApiOperation(value = "获取分页列表")
    @GetMapping("/{page}/{limit}")
    public Result index(@ApiParam(name = "page", value = "当前页码", required = true)
                        @PathVariable Long page,
                        @ApiParam(name = "limit", value = "每页记录数", required = true)
                        @PathVariable Long limit,
                        @ApiParam(name = "commodityTypesQueryVo", value = "查询对象", required = false)
                        CommodityTypesQueryVo commodityTypesQueryVo) {
        Page<CommodityTypes> pageParam = new Page<>(page, limit);
        IPage<CommodityTypes> pageModel = commodityTypesService.selectPage(pageParam, commodityTypesQueryVo);
        return Result.ok(pageModel);
    }

    @PreAuthorize("hasAuthority('bnt.commodityTypes.list')")
    @ApiOperation(value = "查询列表")
    @GetMapping("/list")
    public Result list(@ApiParam(name = "commodityTypesQueryVo", value = "查询对象", required = false)
                       CommodityTypesQueryVo commodityTypesQueryVo) {
        List<CommodityTypes> list = commodityTypesService.queryList(commodityTypesQueryVo);
        return Result.ok(list);
    }

    @ApiOperation(value = "获取全部分类")
    @GetMapping("/findAll")
    @CrossOrigin
    public Result findAllCommodityTypes(HttpServletRequest request) {
        // 检查请求来源
        if (!originCheckUtil.checkOrigin(request)) {
            return Result.fail("请求来源不合法");
        }

        String cacheKey = "commodity:types:all";
        String lockKey = "commodity:types:all:lock";

        // 【1】先尝试从缓存读取
        Object cacheVal = redisTemplate.opsForValue().get(cacheKey);
        if (cacheVal instanceof String) {
            try {
                List<CommodityTypes> cachedList = JSON.parseArray((String) cacheVal, CommodityTypes.class);
                return Result.ok(cachedList);
            } catch (Exception e) {
                // 解析失败则忽略，继续查库
            }
        }

        // 【2】缓存未命中，尝试获取分布式锁防止击穿
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 10, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                // 双重检查
                cacheVal = redisTemplate.opsForValue().get(cacheKey);
                if (cacheVal instanceof String) {
                    try {
                        List<CommodityTypes> cachedList = JSON.parseArray((String) cacheVal, CommodityTypes.class);
                        return Result.ok(cachedList);
                    } catch (Exception e) {
                        // 忽略
                    }
                }

                QueryWrapper queryWrapper = new QueryWrapper();
                queryWrapper.ne("id","2036519036380106754");
                // 查询数据库
                List<CommodityTypes> list = commodityTypesService.list(queryWrapper);

                // 写入缓存 + 随机过期时间防雪崩（30~40分钟）
                int expireTime = 30 + (int)(Math.random() * 10); // 30-40分钟
                redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(list), expireTime, TimeUnit.MINUTES);

                return Result.ok(list);
            } finally {
                // 释放锁
                redisTemplate.delete(lockKey);
            }
        } else {
            // 获取锁失败 → 降级：等待后尝试读缓存或直接返回查询结果
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 再次尝试读缓存
            cacheVal = redisTemplate.opsForValue().get(cacheKey);
            if (cacheVal instanceof String) {
                try {
                    List<CommodityTypes> cachedList = JSON.parseArray((String) cacheVal, CommodityTypes.class);
                    return Result.ok(cachedList);
                } catch (Exception e) {
                    // 忽略
                }
            }

            // 最终降级：直接查数据库
            List<CommodityTypes> list = commodityTypesService.list();
            return Result.ok(list);
        }
    }


    @PreAuthorize("hasAuthority('bnt.commodityTypes.list')")
    @ApiOperation(value = "获取素材分类描述")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable String id) {
        CommodityTypes commodityTypes = commodityTypesService.getById(id);
        return Result.ok(commodityTypes);
    }


    @PreAuthorize("hasAuthority('bnt.commodityTypes.list')")
    @ApiOperation(value = "获取素材分类描述集合")
    @PostMapping("/getByIds")
    public Result getByIds(@RequestBody List<String> idList) {
        List<CommodityTypes> list = commodityTypesService.getByIds(idList);
        return Result.ok(list);
    }

    @PreAuthorize("hasAuthority('bnt.commodityTypes.add')")
    @ApiOperation(value = "保存素材分类描述")
    @PostMapping("/save")
    public Result save(@RequestBody CommodityTypes commodityTypes) {
        commodityTypesService.save(commodityTypes);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.commodityTypes.update')")
    @ApiOperation(value = "更新素材分类描述")
    @PutMapping("/update")
    public Result updateById(@RequestBody CommodityTypes commodityTypes) {
        commodityTypesService.updateById(commodityTypes);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.commodityTypes.remove')")
    @ApiOperation(value = "删除素材分类描述")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable String id) {
        commodityTypesService.removeById(id);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.commodityTypes.remove')")
    @ApiOperation(value = "根据id列表删除")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<String> idList) {
        boolean b = commodityTypesService.removeByIds(idList);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

 /*   @ApiOperation(value = "批量上传本地文件到OSS")
    @PostMapping("/uploadLocalFilesToOss")
    @CrossOrigin
    public Result uploadLocalFilesToOss() {
        String localDir = "D:/project/resource/movie/img/photos";
        File dir = new File(localDir);

        if (!dir.exists() || !dir.isDirectory()) {
            return Result.fail("目录不存在: " + localDir);
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return Result.fail("目录下没有文件: " + localDir);
        }

        // 启动线程在后台运行
        new Thread(() -> {
            List<Map<String, String>> successList = new ArrayList<>();
            List<Map<String, String>> existingList = new ArrayList<>();
            List<String> failList = new ArrayList<>();

            // 先获取OSS中已存在的文件列表
            List<String> existingFiles = ossUtil.listFiles("img/photos");
            System.out.println("OSS中已存在 " + existingFiles.size() + " 个文件");

            for (File file : files) {
                if (file.isFile()) {
                    try {
                        // 检查文件是否已存在
                        if (existingFiles.contains(file.getName())) {
                            Map<String, String> map = new HashMap<>();
                            map.put("fileName", file.getName());
                            map.put("status", "已存在");
                            existingList.add(map);
                            System.out.println("文件已存在，跳过: " + file.getName());
                        } else {
                            String url = ossUtil.uploadFile(file, "img/photos");
                            if (url != null) {
                                Map<String, String> map = new HashMap<>();
                                map.put("fileName", file.getName());
                                map.put("ossUrl", url);
                                map.put("status", "上传成功");
                                successList.add(map);
                                System.out.println("上传成功: " + file.getName());
                            } else {
                                failList.add(file.getName() + ": 上传失败");
                                System.out.println("上传失败: " + file.getName());
                            }
                        }
                        System.out.println("处理进度：成功 " + successList.size() + " 个，已存在 " + existingList.size() + " 个，失败 " + failList.size() + " 个");
                    } catch (Exception e) {
                        failList.add(file.getName() + ": " + e.getMessage());
                        System.out.println("上传异常: " + file.getName() + " - " + e.getMessage());
                    }
                }
            }

            // 可以在这里添加日志记录或其他处理
            System.out.println("OSS上传完成：成功 " + successList.size() + " 个，已存在 " + existingList.size() + " 个，失败 " + failList.size() + " 个");
        }).start();

        return Result.ok("文件上传已在后台开始执行，请稍后查看结果");
    }*/

    /*@ApiOperation(value = "文件传输接口")
    @PostMapping("/uploadFile")
    @CrossOrigin
    public Result uploadFile(@ApiParam(name = "file", value = "上传文件", required = true) @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }

        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                return Result.fail("文件名不能为空");
            }

            String savePath = ossProperties.getLocPath()+"photos/";
            File saveDir = new File(savePath);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }

            File destFile = new File(savePath + fileName);
            file.transferTo(destFile);

            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("savePath", savePath + fileName);
            result.put("url", "/img/" + fileName);

            return Result.ok(result);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }*/

 /*   @ApiOperation(value = "批量读取本地文件并上传")
    @PostMapping("/batchUploadLocalFiles")
    @CrossOrigin
    public Result batchUploadLocalFiles() {
        String localDir = "D:/project/resource/movie/img/photos";
        File dir = new File(localDir);

        if (!dir.exists() || !dir.isDirectory()) {
            return Result.fail("目录不存在: " + localDir);
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return Result.fail("目录下没有文件: " + localDir);
        }

        // 获取当前服务器地址
        String serverUrl = "http://47.103.223.115:5240";

        // 启动新线程执行批量上传
        new Thread(() -> {
            List<Map<String, Object>> successList = new ArrayList<>();
            List<Map<String, Object>> failList = new ArrayList<>();
            int skipCount = 0;

            // 先获取已存在的文件列表
            List<String> existingFiles = new ArrayList<>();
            try {
                String existingResult = callGetPhotosFilesApi(serverUrl);
                Map<String, Object> existingResultMap = JSON.parseObject(existingResult, Map.class);
                if (existingResultMap != null && existingResultMap.get("code") != null && (Integer) existingResultMap.get("code") == 200) {
                    Object data = existingResultMap.get("data");
                    if (data instanceof List) {
                        existingFiles = (List<String>) data;
                        System.out.println("已获取已存在文件列表，共 " + existingFiles.size() + " 个文件");
                    }
                }
            } catch (Exception e) {
                System.out.println("获取已存在文件列表失败: " + e.getMessage());
            }

            for (File file : files) {
                if (file.isFile()) {
                    // 检查文件是否已存在
                    if (existingFiles.contains(file.getName())) {
                        System.out.println("跳过已存在文件: " + file.getName());
                        skipCount++;
                        continue;
                    }

                    try {
                        String result = callUploadFileApi(serverUrl, file);
                        Map<String, Object> resultMap = JSON.parseObject(result, Map.class);

                        if (resultMap != null && resultMap.get("code") != null && (Integer) resultMap.get("code") == 200) {
                            successList.add((Map<String, Object>) resultMap.get("data"));
                            System.out.println("上传成功: " + file.getName());
                        } else {
                            Map<String, Object> failResult = new HashMap<>();
                            String errorMsg = resultMap != null ? (resultMap.get("message") != null ? resultMap.get("message").toString() : "未知错误") : "未知错误";
                            failResult.put("fileName", file.getName());
                            failResult.put("error", errorMsg);
                            failResult.put("response", result);
                            failList.add(failResult);
                            System.out.println("上传失败: " + file.getName() + " - " + errorMsg);
                            System.out.println("  响应内容: " + result);
                        }
                    } catch (Exception e) {
                        Map<String, Object> failResult = new HashMap<>();
                        failResult.put("fileName", file.getName());
                        failResult.put("error", e.getMessage());
                        failList.add(failResult);
                        System.out.println("上传失败: " + file.getName() + " - " + e.getMessage());
                        System.out.println("  异常详情:");
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("=== 批量上传完成 ===");
            System.out.println("总计: " + files.length + " 个文件");
            System.out.println("跳过: " + skipCount + " 个");
            System.out.println("成功: " + successList.size() + " 个");
            System.out.println("失败: " + failList.size() + " 个");
            if (!failList.isEmpty()) {
                System.out.println("失败文件列表:");
                for (int i = 0; i < failList.size(); i++) {
                    Map<String, Object> fail = failList.get(i);
                    System.out.println((i + 1) + ". " + fail.get("fileName") + ": " + fail.get("error"));
                    if (fail.containsKey("response")) {
                        System.out.println("   响应内容: " + fail.get("response"));
                    }
                }
            }
        }).start();

        return Result.ok("批量上传任务已启动，共 " + files.length + " 个文件将在后台处理");
    }*/


 /*   @ApiOperation(value = "获取photos目录下的所有文件名称")
    @GetMapping("/getPhotosFiles")
    @CrossOrigin
    public Result getPhotosFiles() {
        String photosPath = ossProperties.getLocPath() + "photos/";
        File dir = new File(photosPath);

        if (!dir.exists() || !dir.isDirectory()) {
            return Result.fail("目录不存在: " + photosPath);
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return Result.ok(new ArrayList<>());
        }

        List<String> fileNames = new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) {
                fileNames.add(file.getName());
            }
        }

        return Result.ok(fileNames);
    }*/

    private String callGetPhotosFilesApi(String serverUrl) throws Exception {
        java.net.URL url = new java.net.URL(serverUrl + "/business/commodityTypes/getPhotosFiles");
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(
                        responseCode >= 200 && responseCode < 300 ? connection.getInputStream() : connection.getErrorStream(),
                        "UTF-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    private String callUploadFileApi(String serverUrl, File file) throws Exception {
        java.net.URL url = new java.net.URL(serverUrl + "/business/commodityTypes/uploadFile");
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("Connection", "Keep-Alive");

        // 计算内容长度
        long contentLength = 0;
        String partHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n" +
                "Content-Type: application/octet-stream\r\n" +
                "Content-Transfer-Encoding: binary\r\n\r\n";
        contentLength += partHeader.getBytes("UTF-8").length;
        contentLength += file.length();
        contentLength += ("\r\n--" + boundary + "--\r\n").getBytes("UTF-8").length;
        connection.setRequestProperty("Content-Length", String.valueOf(contentLength));

        try (java.io.OutputStream outputStream = connection.getOutputStream()) {
            // 写入头部
            outputStream.write(partHeader.getBytes("UTF-8"));
            outputStream.flush();

            // 写入文件内容
            try (java.io.FileInputStream fileInputStream = new java.io.FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

            // 写入结束边界
            outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes("UTF-8"));
            outputStream.flush();
        }

        // 读取响应
        int responseCode = connection.getResponseCode();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(
                        responseCode >= 200 && responseCode < 300 ? connection.getInputStream() : connection.getErrorStream(),
                        "UTF-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }



}
