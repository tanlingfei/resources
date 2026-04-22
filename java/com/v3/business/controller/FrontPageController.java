package com.v3.business.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.v3.business.model.CommodityCommodityinfos;
import com.v3.business.model.CommodityDownloadLink;
import com.v3.business.model.CommodityTypes;
import com.v3.business.service.CommodityCommodityinfosService;
import com.v3.business.service.CommodityDownloadLinkService;
import com.v3.business.service.CommodityTypesService;
import com.v3.common.result.Result;
import com.v3.common.utils.OriginCheckUtil;
import com.v3.common.utils.OssProperties;
import com.v3.system.exception.LanfException;
import com.v3.system.utils.DeviceIdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping
public class FrontPageController {
    private static final int DEFAULT_LIMIT = 50;
    private static final SimpleDateFormat CURSOR_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private CommodityCommodityinfosService commodityCommodityinfosService;
    @Autowired
    private CommodityTypesService commodityTypesService;
    @Autowired
    private CommodityDownloadLinkService commodityDownloadLinkService;
    @Autowired
    private OssProperties ossProperties;
    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private OriginCheckUtil originCheckUtil;


    @GetMapping("/")
    public String index(@RequestParam(value = "name", required = false) String name, Model model, HttpServletRequest request) {
        // 检查请求来源
        model.addAttribute("name", defaultString(name));
        model.addAttribute("typesList", queryTypesListData());
        model.addAttribute("pageData", queryIndexData(name, null, null, DEFAULT_LIMIT));
        model.addAttribute("ctxPath", ossProperties.getPrefix());
        return "index";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable String id, Model model,HttpServletRequest request) {
        DetailPageData detailData = queryDetailData(id);
        CommodityCommodityinfos product = detailData.getProduct();
        if (product == null) {
            model.addAttribute("product", null);
            return "productDetail";
        }
        //保存访问了分享页面的记录
        DeviceIdUtils.generateAndSaveDeviceId(request, id,"detailLog");
        model.addAttribute("product", product);
        model.addAttribute("downloadLinks", detailData.getDownloadLinks());
        model.addAttribute("shareLink", detailData.getShareLink());
        return "productDetail";
    }

    @GetMapping("/types")
    public String getTypesPage(Model model,HttpServletRequest request, @RequestParam(value = "deviceId", required = false) String deviceId) {
        return "getTypes";
    }

    @GetMapping("/care")
    public String wechatPage() {
        return "wechatQrCode";
    }

    @PostMapping("/saveClipboard")
    @ResponseBody
    public Result saveClipboard(String content, String deviceId, HttpServletRequest request) {
        // 检查请求来源
        if (!originCheckUtil.checkOrigin(request)) {
            throw new LanfException(4444,"请求来源不合法");
        }

        if(StringUtils.isEmpty(content)){
            throw new LanfException(4444,"content  is required");
        }

        if(StringUtils.isEmpty(deviceId)){
            throw new LanfException(4444,"deviceId  is required");
        }

        String ids [] = content.split("#");

        if(ids.length < 2){
            return Result.fail("口令格式错误");
        }

        String code = ids [0];
        //检查code 是否正确
        CommodityTypes commodityTypes = commodityTypesService.getByCodeCache();
        if(commodityTypes!=null){
            if(!code.equals(commodityTypes.getSeconds())){
                return Result.fail("口令错误");
            }
        }


        System.out.println("接受content=" + content);

        // 存到redis：key = deviceId, value = content 10分钟过期时间
        DeviceIdUtils.save(redisTemplate,request,content,deviceId);

     /*   redisTemplate.opsForValue().set("clipboard:" + DeviceIdUtils.getDeviceId(request,deviceId), content, 60 * 10, TimeUnit.SECONDS);*/

        return Result.ok();
    }

    @GetMapping("/getClipboard")
    @ResponseBody
    public Result<String> getClipboard(String deviceId, HttpServletRequest request) {
        if (!originCheckUtil.checkOrigin(request)) {
            throw new LanfException(4444,"请求来源不合法");
        }

        if(StringUtils.isEmpty(deviceId)){
            throw new LanfException(4444,"deviceId is required");
        }
        String content = null;
        // First try to get data by deviceId if provided
        if (org.springframework.util.StringUtils.hasText(deviceId) && !"13621215240".equals(deviceId)) {
            content = DeviceIdUtils.getMyDataByDeviceId(redisTemplate, deviceId);
        }

        if(StringUtils.isEmpty(content)){
            content = DeviceIdUtils.getMyData(redisTemplate,request);
        }


        return Result.ok(content);
    }

    @GetMapping("/page/index/data")
    @ResponseBody
    public Result<Map<String, Object>> indexData(@RequestParam(value = "name", required = false) String name,
                                                 @RequestParam(value = "cursorCreateTime", required = false) String cursorCreateTime,
                                                 @RequestParam(value = "cursorId", required = false) String cursorId,
                                                 @RequestParam(value = "limit", required = false) Integer limit, HttpServletRequest request) {
        // 检查请求来源
        if (!originCheckUtil.checkOrigin(request)) {
            throw new LanfException(4444,"请求来源不合法");
        }
        return Result.ok(queryIndexData(name, cursorCreateTime, cursorId, limit == null ? DEFAULT_LIMIT : limit));
    }

    private Map<String, Object> queryIndexData(String name, String cursorCreateTime, String cursorId, int limit) {
        String cacheKey = String.format("front:index:data:%s:%s:%s:%d",
                safe(name), safe(cursorCreateTime), safe(cursorId), limit);
        String lockKey = cacheKey + ":lock";
        Object cacheVal = redisTemplate.opsForValue().get(cacheKey);
        if (cacheVal instanceof String) {
            try {
                return JSON.parseObject((String) cacheVal, Map.class);
            } catch (Exception ignored) {
            }
        }

        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                Object secondCheck = redisTemplate.opsForValue().get(cacheKey);
                if (secondCheck instanceof String) {
                    try {
                        return JSON.parseObject((String) secondCheck, Map.class);
                    } catch (Exception ignored) {
                    }
                }
                Map<String, Object> data = doQueryIndexData(name, cursorCreateTime, cursorId, limit);
                int expire = 30 + (int) (Math.random() * 10);
                redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(data), expire, TimeUnit.MINUTES);
                return data;
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Object retryCache = redisTemplate.opsForValue().get(cacheKey);
            if (retryCache instanceof String) {
                try {
                    return JSON.parseObject((String) retryCache, Map.class);
                } catch (Exception ignored) {
                }
            }
            return doQueryIndexData(name, cursorCreateTime, cursorId, limit);
        }
    }

    private List<CommodityTypes> queryTypesListData() {
        String cacheKey = "front:index:typesList";
        String lockKey = cacheKey + ":lock";
        Object cacheVal = redisTemplate.opsForValue().get(cacheKey);
        if (cacheVal instanceof String) {
            try {
                return JSON.parseArray((String) cacheVal, CommodityTypes.class);
            } catch (Exception ignored) {
            }
        }

        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                Object secondCheck = redisTemplate.opsForValue().get(cacheKey);
                if (secondCheck instanceof String) {
                    try {
                        return JSON.parseArray((String) secondCheck, CommodityTypes.class);
                    } catch (Exception ignored) {
                    }
                }
                QueryWrapper queryWrapper = new QueryWrapper();
                queryWrapper.ne("id","2036519036380106754");
                List<CommodityTypes> data = commodityTypesService.list(queryWrapper);
                int expire = 30 + (int) (Math.random() * 10);
                redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(data), expire, TimeUnit.MINUTES);
                return data;
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Object retryCache = redisTemplate.opsForValue().get(cacheKey);
            if (retryCache instanceof String) {
                try {
                    return JSON.parseArray((String) retryCache, CommodityTypes.class);
                } catch (Exception ignored) {
                }
            }
            return commodityTypesService.list();
        }
    }

    private DetailPageData queryDetailData(String id) {
        String cacheKey = "front:detail:data:" + safe(id);
        String lockKey = cacheKey + ":lock";
        Object cacheVal = redisTemplate.opsForValue().get(cacheKey);
        if (cacheVal instanceof String) {
            try {
                return JSON.parseObject((String) cacheVal, DetailPageData.class);
            } catch (Exception ignored) {
            }
        }

        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                Object secondCheck = redisTemplate.opsForValue().get(cacheKey);
                if (secondCheck instanceof String) {
                    try {
                        return JSON.parseObject((String) secondCheck, DetailPageData.class);
                    } catch (Exception ignored) {
                    }
                }
                DetailPageData data = doQueryDetailData(id);
                int expire = data.getProduct() == null ? 5 : (30 + (int) (Math.random() * 10));
                redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(data), expire, TimeUnit.MINUTES);
                return data;
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Object retryCache = redisTemplate.opsForValue().get(cacheKey);
            if (retryCache instanceof String) {
                try {
                    return JSON.parseObject((String) retryCache, DetailPageData.class);
                } catch (Exception ignored) {
                }
            }
            return doQueryDetailData(id);
        }
    }

    private DetailPageData doQueryDetailData(String id) {
        DetailPageData data = new DetailPageData();
        CommodityCommodityinfos product = commodityCommodityinfosService.getById(id);
        if (product == null) {
            data.setProduct(null);
            data.setDownloadLinks(new ArrayList<>());
            data.setShareLink("");
            return data;
        }
        normalizeImage(product);
        product.setUsername("admin");
        List<CommodityDownloadLink> links = new ArrayList<>();
        String shareLink = "";
        if ("1".equals(product.getIsUpdate()) || (product.getLikes() != null && product.getLikes() == 9999)) {
            links = commodityDownloadLinkService.getByCommodityId(id);
        } else {
            CommodityTypes commodityTypes = commodityTypesService.getById("2036519036380106754");
            if (commodityTypes != null) {
                shareLink = commodityTypes.getSeconds() + "#" + id;
            }
        }
        data.setProduct(product);
        data.setDownloadLinks(links);
        data.setShareLink(shareLink);
        return data;
    }

    private Map<String, Object> doQueryIndexData(String name, String cursorCreateTime, String cursorId, int limit) {
        QueryWrapper<CommodityCommodityinfos> wrapper = new QueryWrapper<>();
        wrapper.select("id", "name", "img", "details", "create_time");
        if (hasText(name)) {
            /*wrapper.like("name", name.trim());*/
            wrapper.apply("MATCH(name) AGAINST({0} IN BOOLEAN MODE)", name);
        }

        Date cursorDate = parseDate(cursorCreateTime);
        if (cursorDate != null && hasText(cursorId)) {
            wrapper.and(w -> w.lt("create_time", cursorDate)
                    .or()
                    .eq("create_time", cursorDate).lt("id", cursorId));
        }
        wrapper.orderByDesc("create_time").orderByDesc("id");
        wrapper.last("limit " + limit);
        List<CommodityCommodityinfos> records = commodityCommodityinfosService.list(wrapper);
        /*records.forEach(this::normalizeImage);*/

        String nextCursorCreateTime = null;
        String nextCursorId = null;
        if (!records.isEmpty()) {
            CommodityCommodityinfos last = records.get(records.size() - 1);
            if (last.getCreateTime() != null) {
                nextCursorCreateTime = CURSOR_TIME_FORMAT.format(last.getCreateTime());
            }
            nextCursorId = last.getId();
        }

        long total = getTotalCount(name);

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("nextCursorCreateTime", nextCursorCreateTime);
        data.put("nextCursorId", nextCursorId);
        data.put("total", total);
        return data;
    }

    private long getTotalCount(String name) {
        String cacheKey = String.format("front:index:count:%s", safe(name));
        String lockKey = cacheKey + ":lock";
        Object cacheVal = redisTemplate.opsForValue().get(cacheKey);
        if (cacheVal instanceof String) {
            try {
                return Long.parseLong((String) cacheVal);
            } catch (Exception ignored) {
            }
        }

        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                Object secondCheck = redisTemplate.opsForValue().get(cacheKey);
                if (secondCheck instanceof String) {
                    try {
                        return Long.parseLong((String) secondCheck);
                    } catch (Exception ignored) {
                    }
                }
                QueryWrapper<CommodityCommodityinfos> countWrapper = new QueryWrapper<>();
                if (hasText(name)) {
                    /*countWrapper.like("name", name.trim());*/
                    countWrapper.apply("MATCH(name) AGAINST({0} IN BOOLEAN MODE)", name);
                }
                long total = commodityCommodityinfosService.count(countWrapper);
                int expire = 30 + (int) (Math.random() * 10);
                redisTemplate.opsForValue().set(cacheKey, String.valueOf(total), expire, TimeUnit.MINUTES);
                return total;
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Object retryCache = redisTemplate.opsForValue().get(cacheKey);
            if (retryCache instanceof String) {
                try {
                    return Long.parseLong((String) retryCache);
                } catch (Exception ignored) {
                }
            }
            QueryWrapper<CommodityCommodityinfos> countWrapper = new QueryWrapper<>();
            if (hasText(name)) {
                /*countWrapper.like("name", name.trim());*/
                countWrapper.apply("MATCH(name) AGAINST({0} IN BOOLEAN MODE)", name);
            }
            return commodityCommodityinfosService.count(countWrapper);
        }
    }

    private void normalizeImage(CommodityCommodityinfos info) {
        if (info == null || !hasText(info.getImg())) {
            return;
        }
        if (!info.getImg().startsWith("http")) {
            info.setImg(ossProperties.getPrefix() + info.getImg());
        }
    }

    private Date parseDate(String text) {
        if (!hasText(text)) {
            return null;
        }
        try {
            return CURSOR_TIME_FORMAT.parse(text);
        } catch (ParseException e) {
            return null;
        }
    }

    private boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }

    private String defaultString(String text) {
        return text == null ? "" : text;
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }

    public static class DetailPageData {
        private CommodityCommodityinfos product;
        private List<CommodityDownloadLink> downloadLinks;
        private String shareLink;

        public CommodityCommodityinfos getProduct() {
            return product;
        }

        public void setProduct(CommodityCommodityinfos product) {
            this.product = product;
        }

        public List<CommodityDownloadLink> getDownloadLinks() {
            return downloadLinks;
        }

        public void setDownloadLinks(List<CommodityDownloadLink> downloadLinks) {
            this.downloadLinks = downloadLinks;
        }

        public String getShareLink() {
            return shareLink;
        }

        public void setShareLink(String shareLink) {
            this.shareLink = shareLink;
        }
    }
}

