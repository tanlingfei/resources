package com.v3.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.v3.business.model.CommodityTypes;
import com.v3.business.vo.CommodityTypesQueryVo;

import java.util.List;
import java.util.Map;

/**
 * @author administrator
* @微信 zzia6789
* @官网 https://mf5240.asia
 * @version 1.0
 * @description 素材分类描述 Service接口
 * @date 2025-04-06 23:05:25
 */
public interface CommodityTypesService extends IService<CommodityTypes> {
    IPage<CommodityTypes> selectPage(Page<CommodityTypes> pageParam, CommodityTypesQueryVo queryVo);

    List<CommodityTypes> queryList(CommodityTypesQueryVo queryVo);

    List<Map> getAllTypesFirst();

    public boolean save(CommodityTypes commodityTypes);

    public boolean updateById(CommodityTypes commodityTypes);

    public CommodityTypes getById(String id);

    public List<CommodityTypes> getByIds(List<String> ids);

    public CommodityTypes getByCodeCache();
}
