import request from "@/request";
const api_name = '/admin/system/index'
// 登录
export const login = (data) => {
  return request({
    url: `${api_name}/login`,
    method: "post",
    data,
  });
};

// 登出
export const logout = () => {
  return request({
    url: `${api_name}/logout`,
    method: "post",
  });
};

