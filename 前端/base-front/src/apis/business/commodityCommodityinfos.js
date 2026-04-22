import request from '@/request'
import axios from 'axios'
import config from '@/request/config'

const api_name = '/business/commodityCommodityinfos'
export default {
    listPage(page, limit, searchObj) {
        return request({
            url: `${api_name}/${page}/${limit}`,
            method: 'get',
            params: searchObj // url查询字符串或表单键值对
        })
    },
    getAllCommodityCommodityinfos() {
        return request({
            url: `${api_name}/findAll`,
            method: 'get'
        })
    },
    getById(id) {
        return request({
            url: `${api_name}/get/${id}`,
            method: 'get'
        })
    },
    getByIds(ids) {
        return request({
            url: `${api_name}/getByIds`,
            method: 'post',
            data: ids
        })
    },
    save(data) {
        return request({
            url: `${api_name}/save`,
            method: 'post',
            data: data
        })
    },
    batchSave(data, config = {}) {
        return request({
            url: `${api_name}/batchSave`,
            method: 'post',
            data: data,
            headers: {
                'Content-Type': 'multipart/form-data'
            },
            ...config // 合并额外的配置，如onUploadProgress
        })
    },
    update(data) {
        return request({
            url: `${api_name}/update`,
            method: 'put',
            data: data
        })
    },

    remove(ids) {
        return request({
            url: `${api_name}/batchRemove`,
            method: "delete",
            data: ids
        })
    },
    
    // 更新下载次数的专用接口
    updateDowns(id, downs) {
        return request({
            url: `${api_name}/updateDowns/${id}`,
            method: 'put',
            data: { downs }
        })
    },
    
    // 获取收藏排行榜
    getCollectTop() {
        return request({
            url: `${api_name}/collectTop`,
            method: 'get'
        })
    },
    
    // 更新收藏次数的专用接口
    updateLikes(id, likes) {
        return request({
            url: `${api_name}/updateLikes/${id}`,
            method: 'put',
            data: { likes }
        })
    },
    
    // 更新浏览次数的专用接口
    updateViews(id, views) {
        return request({
            url: `${api_name}/updateViews/${id}`,
            method: 'put',
            data: { views }
        })
    },

    // 导出全部数据为Excel（CSV），支持下载进度
    exportAll(searchObj, extraConfig = {}) {
        const instance = axios.create({ ...config })
        const token = localStorage.getItem('pm_token')
        if (token) {
            instance.defaults.headers.token = token
        }
        return instance({
            url: `${api_name}/export`,
            method: 'get',
            params: searchObj,
            responseType: 'blob',
            ...extraConfig
        })
    },

    // 清除所有Redis缓存
    clearAllCache() {
        return request({
            url: `${api_name}/clearAllCache`,
            method: 'delete'
        })
    }

}