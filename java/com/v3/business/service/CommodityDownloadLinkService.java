package com.v3.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.v3.business.model.CommodityDownloadLink;

import java.util.List;

/**
 * @author administrator
* @微信 zzia6789
* @官网 https://mf5240.asia
 * @version 1.0
 * @description 素材下载地址表 Service接口
 * @date 2025-01-07
 */
public interface CommodityDownloadLinkService extends IService<CommodityDownloadLink> {
    /**
     * 根据素材ID查询下载地址列表
     * @param commodityId 素材ID
     * @return 下载地址列表
     */
    List<CommodityDownloadLink> getByCommodityId(String commodityId);
    
    /**
     * 根据素材ID删除下载地址
     * @param commodityId 素材ID
     * @return 是否成功
     */
    boolean deleteByCommodityId(String commodityId);
    
    /**
     * 保存或更新下载链接
     * @param downloadLink 下载链接对象
     * @return 是否成功
     */
    boolean saveOrUpdate(CommodityDownloadLink downloadLink);
}

