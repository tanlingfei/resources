package com.v3.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.v3.business.mapper.CommodityDownloadLinkMapper;
import com.v3.business.model.CommodityDownloadLink;
import com.v3.business.service.CommodityDownloadLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author administrator
* @微信 zzia6789
* @官网 https://mf5240.asia
 * @version 1.0
 * @description 素材下载地址表 Service实现类
 * @date 2025-01-07
 */
@Transactional
@Service
public class CommodityDownloadLinkServiceImpl extends ServiceImpl
        <CommodityDownloadLinkMapper, CommodityDownloadLink> implements CommodityDownloadLinkService {
    @Autowired
    private CommodityDownloadLinkMapper commodityDownloadLinkMapper;

    @Override
    public List<CommodityDownloadLink> getByCommodityId(String commodityId) {
        QueryWrapper<CommodityDownloadLink> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("commodity_id", commodityId);
        return this.list(queryWrapper);
    }

    @Override
    public boolean deleteByCommodityId(String commodityId) {
        QueryWrapper<CommodityDownloadLink> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("commodity_id", commodityId);
        return this.remove(queryWrapper);
    }
    
    @Override
    public boolean saveOrUpdate(CommodityDownloadLink downloadLink) {
        if (downloadLink.getId() != null && !downloadLink.getId().isEmpty()) {
            // 如果有ID，执行更新操作
            return this.updateById(downloadLink);
        } else {
            // 如果没有ID，执行插入操作
            return this.save(downloadLink);
        }
    }
}

