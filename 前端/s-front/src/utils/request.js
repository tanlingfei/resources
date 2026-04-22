// src/utils/request.js

import axios from 'axios'
import { ElMessage } from 'element-plus' // 按需引入 UI 提示库（可选）
import router from '@/router' // 按需引入路由（用于跳转登录）

// 创建 axios 实例
const service = axios.create({
 /*  baseURL: 'http://47.103.223.115:5240',*/
    baseURL: 'http://localhost:5240',
   /* baseURL: '/api',*/
    timeout: 60*60*1000,
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        // 统一添加 Token（根据实际项目调整）
        const token = localStorage.getItem('fm_token')
        if (token) {
            config.headers.token = token
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        // 直接返回后端接口数据（根据实际数据结构调整）
        if (response.data && (response.data.code === 50014 || response.data.code === 208 )) {
            ElMessage.error('请先登录')
            router.push('/login')
            return Promise.reject(new Error('请先登录'))
        }
        return response.data
    },
    error => {
        // 统一错误处理
        if (error.response) {
            switch (error.response.status) {
                case 401:
                    ElMessage.error('登录已过期，请重新登录')
                    router.push('/login')
                    break
                case 403:
                    ElMessage.error('没有权限')
                    break
                case 500:
                    ElMessage.error('服务器错误')
                    break
                default:
                    ElMessage.error(error.response.data.message || '请求失败')
            }
        } else {
            ElMessage.error('网络连接异常')
        }
        return Promise.reject(error)
    }
)

// 封装 GET/POST 通用方法
export function get(url, params = {}, successCallback, errorCallback) {
    return service({ method: 'get', url, params })
        .then(res => {
            if (successCallback) successCallback(res)
            return res // 保证链式调用
        })
        .catch(err => {
            if (errorCallback) errorCallback(err)
            return Promise.reject(err) // 继续传递错误
        })
}

export function post(url, data = {}, successCallback, errorCallback) {
    return service({ method: 'post', url, data })
        .then(res => {
            if (successCallback) successCallback(res)
            return res
        })
        .catch(err => {
            if (errorCallback) errorCallback(err)
            return Promise.reject(err)
        })
}

export function put(url, data = {}, successCallback, errorCallback) {
    return service({ method: 'put', url, data })
        .then(res => {
            if (successCallback) successCallback(res)
            return res
        })
        .catch(err => {
            if (errorCallback) errorCallback(err)
            return Promise.reject(err)
        })
}

export function del(url, data = {}, successCallback, errorCallback) {
    return service({ method: 'delete', url, data })
        .then(res => {
            if (successCallback) successCallback(res)
            return res
        })
        .catch(err => {
            if (errorCallback) errorCallback(err)
            return Promise.reject(err)
        })
}

