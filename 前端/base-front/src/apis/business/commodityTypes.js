import request from '@/request'

const api_name = '/business/commodityTypes'
export default {
    listPage(page, limit, searchObj) {
        return request({
            url: `${api_name}/${page}/${limit}`,
            method: 'get',
            params: searchObj // url查询字符串或表单键值对
        })
    },
    getAllTypesFirst() {
        return request({
            url: `${api_name}/getAllTypesFirst`,
            method: 'get'
        })
    },
    getAllCommodityTypes(searchObj) {
        return request({
            url: `${api_name}/findAll`,
            method: 'get',
            params: searchObj
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

}
