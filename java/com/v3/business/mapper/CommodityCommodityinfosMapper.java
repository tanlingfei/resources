package com.v3.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.v3.business.model.CommodityCommodityinfos;
import com.v3.business.vo.CommodityCommodityinfosQueryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author administrator
* @微信 zzia6789
* @官网 https://mf5240.asia
 * @version 1.0
 * @description 素材信息表 Mapper层
 * @date 2025-04-06 23:41:35
 */
@Repository
@Mapper
public interface CommodityCommodityinfosMapper extends BaseMapper<CommodityCommodityinfos> {
    IPage<CommodityCommodityinfos> selectPage(Page<CommodityCommodityinfos> page, @Param("vo") CommodityCommodityinfosQueryVo commodityCommodityinfosQueryVo);

    List<CommodityCommodityinfos> queryList(@Param("vo") CommodityCommodityinfosQueryVo commodityCommodityinfosQueryVo);
    
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
    String getTypesById(@Param("id") String id);
}
