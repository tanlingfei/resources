/*
 * 用户管理模块
 */
import request from "../request";

const api_name = '/admin/system/sysUser'
export default {
// 分页查询
    listPage(page, limit, searchObj) {
        return request({
            url: `${api_name}/${page}/${limit}`,
            method: "get",
            params: searchObj
        });
    },
    getAllSysUser() {
        return request({
            url: `${api_name}/list`,
            method: 'get'
        })
    },
// 新增
    save(data) {
        return request({
            url: `${api_name}/save`,
            method: "post",
            data,
        });
    },

    getById(id) {
        return request({
            url: `${api_name}/getUser/${id}`,
            method: "get",
        });
    },

// 编辑
    update(data) {
        return request({
            url: `${api_name}/update`,
            method: "post",
            data,
        });
    },
// 删除
    remove(data) {
        return request({
            url: `${api_name}/batchRemove`,
            method: "delete",
            data,
        });
    }
}
