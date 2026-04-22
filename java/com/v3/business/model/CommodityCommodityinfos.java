package com.v3.business.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.v3.model.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author administrator
* @微信 zzia6789
* @官网 https://mf5240.asia
 * @version 1.0
 * @description 素材信息表 po类
 * @date 2025-04-06 23:41:35
 */
@Data
@ApiModel(description = "素材信息表")
@TableName("commodity_commodityinfos")
public class CommodityCommodityinfos extends BaseEntity {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "素材名称")
    @TableField("name")
    private String name;
    @ApiModelProperty(value = "分类")
    @TableField("types")
    private String types;
    @TableField("types_name")
    private String typesName;

    @ApiModelProperty(value = "收藏数量 这个暂时把他用成了 网盘类型 8888是夸克，  9999是百度 1111是tb百度")
    @TableField("likes")
    private Integer likes;



    @ApiModelProperty(value = "素材文件地址")
    @TableField("img")
    private String img;

    @ApiModelProperty(value = "素材轮播图")
    @TableField("lb_img")
    private String lbImg;

    @ApiModelProperty(value = "素材介绍")
    @TableField("details")
    private String details;

    @ApiModelProperty(value = "用户id")
    @TableField("user_id")
    private String userId;

    @ApiModelProperty(value = "用户名")
    @TableField("username")
    private String username;

    @ApiModelProperty(value = "是否公开")
    @TableField("is_pub")
    private String isPub;

    @ApiModelProperty(value = "是否推荐到首页")
    @TableField("is_recom")
    private String isRecom;

    @ApiModelProperty(value = "浏览量")
    @TableField("views")
    private Integer views;

    @ApiModelProperty(value = "下载量")
    @TableField("downs")
    private Integer downs;

    @ApiModelProperty(value = "素材大小")
    @TableField("size")
    private  long size;

    @ApiModelProperty(value = "是否更新")
    @TableField("is_update")
    private String isUpdate;

    //素材件对象
    @TableField(exist = false)
    private MultipartFile file;

    //轮播图对象
    @TableField(exist = false)
    private MultipartFile fileImg;

    @ApiModelProperty(value = "上传类型 1:本地 2:oss 3：全部")
    @TableField(exist = false)
    private String uploadType;


    @ApiModelProperty(value = "分享类型 1:网盘分享 2:搜索分享")
    @TableField(exist = false)
    private String shareType;


}
