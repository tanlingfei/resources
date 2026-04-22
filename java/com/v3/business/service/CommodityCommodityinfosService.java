package com.v3.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.v3.business.model.CommodityCommodityinfos;
import com.v3.business.vo.CommodityCommodityinfosQueryVo;

import java.util.List;

/**
 * @author administrator
* @微信 zzia6789
* @官网 https://mf5240.asia
 * @version 1.0
 * @description 素材信息表 Service接口
 * @date 2025-04-06 23:41:35
 */
public interface CommodityCommodityinfosService extends IService<CommodityCommodityinfos> {
    IPage<CommodityCommodityinfos> selectPage(Page<CommodityCommodityinfos> pageParam, CommodityCommodityinfosQueryVo queryVo);

    List<CommodityCommodityinfos> queryList(CommodityCommodityinfosQueryVo queryVo);

    public boolean save(CommodityCommodityinfos commodityCommodityinfos);

    public boolean updateById(CommodityCommodityinfos commodityCommodityinfos);

    public CommodityCommodityinfos getById(String id);

    public List<CommodityCommodityinfos> getByIds(List<String> ids);
    
    /**
     * 获取收藏排行榜前100名
     * @return 收藏次数前100的素材列表
     */
    List<CommodityCommodityinfos> getCollectTop();

    /**
     * 根据id只查询types字段
     * @param id 素材id
     * @return types字段值
     */
    String getTypesById(String id);
}
