import request from "@/request";
const api_name = '/admin/system/index'
// 用户基本信息
export const userInfo = (data) => {
  return request({
    url: `${api_name}/info`,
    method: "get",
    data,
  });
};


export const menuTree = (data) => {
  return request({
    url: `${api_name}/menuTree`,
    method: "get",
    data,
  });
};

export const changeProfile = (data) => {
  return request({
    url: "/personal/changeProfile",
    method: "post",
    data,
  });
};
// 修改密码
export const changePsw = (data) => {
  return request({
    url: `${api_name}/changePwd`,
    method: "post",
    data,
  });
};

