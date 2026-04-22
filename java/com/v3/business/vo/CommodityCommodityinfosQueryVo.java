package com.v3.business.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

/**
 * @author administrator
* @微信 zzia6789
* @官网 https://mf5240.asia
 * @version 1.0
 * @description 素材信息表 vo类
 * @date 2025-04-06 23:41:35
 */
@Data
public class CommodityCommodityinfosQueryVo {
    private String name;
    private String sezes;
    private String types;
    private String typesName;
    private java.math.BigDecimal price;
    private java.math.BigDecimal discount;
    private Integer stock;
    private Integer sold;
    private Integer likes;
    private String img;
    private String details;
    private Date createTimeBegin;
    private Date createTimeEnd;
    private Date updateTimeBegin;
    private Date updateTimeEnd;

    private String isPub;
    private String isRecom;

    private String isUpdate;

    private Long page;
    private Long limit;

    private String userId;

    private String isCollect;

    //排序字段
    private String orderCol;

    private String  searchModel;

    // 游标分页：上一页最后一条数据的创建时间和ID
    private String cursorCreateTime;
    private String cursorId;

}

