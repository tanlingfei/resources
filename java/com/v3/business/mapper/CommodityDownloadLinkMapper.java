package com.v3.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.v3.business.model.CommodityDownloadLink;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author administrator
* @微信 zzia6789
* @官网 https://mf5240.asia
 * @version 1.0
 * @description 素材下载地址表 Mapper层
 * @date 2025-01-07
 */
@Repository
@Mapper
public interface CommodityDownloadLinkMapper extends BaseMapper<CommodityDownloadLink> {
}

