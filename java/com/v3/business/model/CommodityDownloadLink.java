package com.v3.business.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.v3.model.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author administrator
* @微信 zzia6789
* @官网 https://mf5240.asia
 * @version 1.0
 * @description 素材下载地址表 po类
 * @date 2025-01-07
 */
@Data
@ApiModel(description = "素材下载地址表")
@TableName("commodity_download_link")
public class CommodityDownloadLink extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "素材id")
    @TableField("commodity_id")
    private String commodityId;
    
    @ApiModelProperty(value = "下载地址名称")
    @TableField("name")
    private String name;
    
    @ApiModelProperty(value = "下载链接")
    @TableField("url")
    private String url;
}

