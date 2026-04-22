package com.v3.business.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.v3.business.mapper.CommodityCommodityinfosMapper;
import com.v3.business.model.CommodityCommodityinfos;
import com.v3.business.service.CommodityCommodityinfosService;
import com.v3.business.vo.CommodityCommodityinfosQueryVo;
import com.v3.common.result.ResultCodeEnum;
import com.v3.system.exception.LanfException;
import com.v3.system.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Calendar;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;


import javax.imageio.ImageIO;

/**
 * @author administrator
* @微信 zzia6789
* @官网 https://mf5240.asia
 * @version 1.0
 * @description 素材信息表 Service实现类
 * @date 2025-04-06 23:41:35
 */
@Transactional
@Service
public class CommodityCommodityinfosServiceImpl extends ServiceImpl
        <CommodityCommodityinfosMapper, CommodityCommodityinfos> implements CommodityCommodityinfosService {
    @Autowired
    private CommodityCommodityinfosMapper commodityCommodityinfosMapper;

    @Autowired
    private FileService fileService;

    @Override
    public IPage<CommodityCommodityinfos> selectPage(Page<CommodityCommodityinfos> pageParam, CommodityCommodityinfosQueryVo commodityCommodityinfosQueryVo) {
        return commodityCommodityinfosMapper.selectPage(pageParam, commodityCommodityinfosQueryVo);
    }

    @Override
    public List<CommodityCommodityinfos> queryList(CommodityCommodityinfosQueryVo commodityCommodityinfosQueryVo) {
        List<CommodityCommodityinfos> result = commodityCommodityinfosMapper.queryList(commodityCommodityinfosQueryVo);
        return result;
    }

    @Override
    public boolean save(CommodityCommodityinfos commodityCommodityinfos) {
        MultipartFile file = commodityCommodityinfos.getFile();
        String filePath = null;
        if (file != null) {
            try {
                commodityCommodityinfos.setSize(file.getSize());
                String newFileName = UUID.randomUUID().toString().replace("-", "");
                if("2".equals(commodityCommodityinfos.getUploadType())) {
                    filePath = fileService.uploadToOss(file, newFileName);
                }else if("1".equals(commodityCommodityinfos.getUploadType())){
                    filePath = fileService.upload(file, newFileName);
                }else{
                    String[] bothResult = fileService.uploadBoth(file, newFileName);
                    filePath = bothResult[0];
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new LanfException(ResultCodeEnum.UPLOAD_ERROR);
            }
        }
        if (filePath != null) {
            commodityCommodityinfos.setImg(filePath);
        }else{
            commodityCommodityinfos.setImg("");
            commodityCommodityinfos.setLbImg("");
        }
        commodityCommodityinfos.setLbImg(filePath);
        String shareType = commodityCommodityinfos.getShareType();
        if("1".equals(shareType)){  //分享类型 ，用于前端判断
            commodityCommodityinfos.setIsUpdate("1");
            commodityCommodityinfos.setLikes(6666);
        }else if("2".equals(shareType)){
            commodityCommodityinfos.setLikes(8888);
            commodityCommodityinfos.setIsUpdate("否");
        }

        int result = this.commodityCommodityinfosMapper.insert(commodityCommodityinfos);
        return result > 0;
    }

    @Override
    public boolean updateById(CommodityCommodityinfos commodityCommodityinfos) {
        MultipartFile file = commodityCommodityinfos.getFile();
        String filePath = null;
        if (file != null) {
            if (file.getSize() > 0) {
                commodityCommodityinfos.setSize(file.getSize());
            }
            try {
                String newFileName = UUID.randomUUID().toString().replace("-", "");
                if("2".equals(commodityCommodityinfos.getUploadType())) {
                    filePath = fileService.uploadToOss(file, newFileName);
                }else if("1".equals(commodityCommodityinfos.getUploadType())){
                    filePath = fileService.upload(file, newFileName);
                }else{
                    String[] bothResult = fileService.uploadBoth(file, newFileName);
                    filePath = bothResult[0];
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new LanfException(ResultCodeEnum.UPLOAD_ERROR);
            }
        }
        if (filePath != null) {
            commodityCommodityinfos.setImg(filePath);
        } else {
            if (!StringUtils.isEmpty(commodityCommodityinfos.getImg())) {
                CommodityCommodityinfos data = this.getById(commodityCommodityinfos.getId());
                commodityCommodityinfos.setImg(data.getImg());
            }
        }
        if (commodityCommodityinfos.getSize() <= 0) {
            CommodityCommodityinfos data = this.getById(commodityCommodityinfos.getId());
            commodityCommodityinfos.setSize(data.getSize());
        }
        if (file != null && !file.isEmpty() && filePath != null) {
            commodityCommodityinfos.setLbImg(filePath);
        } else {
            CommodityCommodityinfos data = this.getById(commodityCommodityinfos.getId());
            commodityCommodityinfos.setLbImg(data.getLbImg());
        }
        int row = this.commodityCommodityinfosMapper.updateById(commodityCommodityinfos);
        if (row <= 0) {
            throw new LanfException(ResultCodeEnum.UPDATE_ERROR);
        }
        return row > 0;
    }

    @Override
    public CommodityCommodityinfos getById(String id) {
        CommodityCommodityinfos commodityCommodityinfos = commodityCommodityinfosMapper.selectById(id);
        return commodityCommodityinfos;
    }

    @Override
    public List<CommodityCommodityinfos> getByIds(List<String> ids) {
        List<CommodityCommodityinfos> list = this.commodityCommodityinfosMapper.selectBatchIds(ids);
        return list;
    }

    @Override
    public List<CommodityCommodityinfos> getCollectTop() {
        return commodityCommodityinfosMapper.getCollectTop();
    }

    @Override
    public String getTypesById(String id) {
        return commodityCommodityinfosMapper.getTypesById(id);
    }

    /**
     * 判断是否为图片文件
     *
     * @param extension 文件扩展名
     * @return 是否为图片
     */
    private boolean isImageFile(String extension) {
        return extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(".png") ||
                extension.equals(".gif") || extension.equals(".bmp") || extension.equals(".webp");
    }

    /**
     * 判断是否为视频文件
     *
     * @param extension 文件扩展名
     * @return 是否为视频
     */
    private boolean isVideoFile(String extension) {
        return extension.equals(".mp4") || extension.equals(".avi") || extension.equals(".mov") ||
                extension.equals(".wmv") || extension.equals(".flv") || extension.equals(".webm");
    }

}
