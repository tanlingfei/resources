package com.v3.business.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.v3.business.mapper.CommodityTypesMapper;
import com.v3.business.model.CommodityTypes;
import com.v3.business.service.CommodityTypesService;
import com.v3.business.vo.CommodityTypesQueryVo;
import com.v3.common.result.ResultCodeEnum;
import com.v3.system.exception.LanfException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author administrator
* @微信 zzia6789
* @官网 https://mf5240.asia
 * @version 1.0
 * @description 素材分类描述 Service实现类
 * @date 2025-04-06 23:05:25
 */
@Transactional
@Service
public class CommodityTypesServiceImpl extends ServiceImpl
        <CommodityTypesMapper, CommodityTypes> implements CommodityTypesService {
    @Autowired
    private CommodityTypesMapper commodityTypesMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public IPage<CommodityTypes> selectPage(Page<CommodityTypes> pageParam, CommodityTypesQueryVo commodityTypesQueryVo) {
        return commodityTypesMapper.selectPage(pageParam, commodityTypesQueryVo);
    }

    @Override
    public List<CommodityTypes> queryList(CommodityTypesQueryVo commodityTypesQueryVo) {
        List<CommodityTypes> result = commodityTypesMapper.queryList(commodityTypesQueryVo);
        return result;
    }

    @Override
    public boolean save(CommodityTypes commodityTypes) {
        int result = this.commodityTypesMapper.insert(commodityTypes);
        return result > 0;
    }

    @Override
    public boolean updateById(CommodityTypes commodityTypes) {
        int row = this.commodityTypesMapper.updateById(commodityTypes);
        if (row <= 0) {
            throw new LanfException(ResultCodeEnum.UPDATE_ERROR);
        }
        return row > 0;
    }

    @Override
    public CommodityTypes getById(String id) {
        CommodityTypes commodityTypes = commodityTypesMapper.selectById(id);
        return commodityTypes;
    }

    @Override
    public CommodityTypes getByCodeCache() {
        String cacheKey = "commodity:types:code:cache";
        String lockKey = "commodity:types:code:cache:lock";

        // 1. 先尝试从缓存获取
        Object cacheVal = redisTemplate.opsForValue().get(cacheKey);
        if (cacheVal instanceof String) {
            try {
                return JSON.parseObject((String) cacheVal, CommodityTypes.class);
            } catch (Exception e) {
                // 解析失败则忽略，继续查库
            }
        }

        // 2. 缓存未命中，尝试获取分布式锁防止缓存击穿
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 10, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                // 3. 双重检查
                cacheVal = redisTemplate.opsForValue().get(cacheKey);
                if (cacheVal instanceof String) {
                    try {
                        return JSON.parseObject((String) cacheVal, CommodityTypes.class);
                    } catch (Exception e) {
                        // 解析失败则忽略，继续查库
                    }
                }

                // 4. 查询数据库
                CommodityTypes commodityTypes = commodityTypesMapper.selectById("2036519036380106754");

                // 5. 存入缓存 + 随机过期时间防止雪崩
                if (commodityTypes != null) {
                    int baseMinutes = 30;
                    int randomOffset = (int)(Math.random() * 10);
                    int expireTime = baseMinutes + randomOffset;
                    redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(commodityTypes), expireTime, TimeUnit.MINUTES);
                }

                return commodityTypes;
            } finally {
                // 6. 释放锁
                try {
                    redisTemplate.delete(lockKey);
                } catch (Exception ignore) {
                }
            }
        } else {
            // 7. 获取锁失败，等待后重试
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 再次尝试读缓存
            cacheVal = redisTemplate.opsForValue().get(cacheKey);
            if (cacheVal instanceof String) {
                try {
                    return JSON.parseObject((String) cacheVal, CommodityTypes.class);
                } catch (Exception e) {
                    // 解析失败则忽略，直接查库
                }
            }

            // 降级：直接查数据库
            return commodityTypesMapper.selectById("2036519036380106754");
        }
    }

    @Override
    public List<CommodityTypes> getByIds(List<String> ids) {
        List<CommodityTypes> list = this.commodityTypesMapper.selectBatchIds(ids);
        return list;
    }

    @Override
    public List<Map> getAllTypesFirst() {
        return this.commodityTypesMapper.getAllTypesFirst();
    }

}
