import request from '@/utils/request'

// 查询农户列表
export function listFarmers(query) {
  return request({
    url: '/assisting/farmers/list',
    method: 'get',
    params: query
  })
}

// 查询农户详细
export function getFarmers(farmersId) {
  return request({
    url: '/assisting/farmers/' + farmersId,
    method: 'get'
  })
}

// 新增农户
export function addFarmers(data) {
  return request({
    url: '/assisting/farmers',
    method: 'post',
    data: data
  })
}

// 修改农户
export function updateFarmers(data) {
  return request({
    url: '/assisting/farmers',
    method: 'put',
    data: data
  })
}

// 删除农户
export function delFarmers(farmersId) {
  return request({
    url: '/assisting/farmers/' + farmersId,
    method: 'delete'
  })
}

/**
 * 查询当前用户有没有进行过农户认证
 * 如果有 就返回审核状态
 * 如果没有 就返回未提交字符串
 * @returns {*}
 */
export function selectIsAuth() {
  return request({
    url: '/assisting/farmers/selectIsAuth',
    method: 'get'
  })
}
