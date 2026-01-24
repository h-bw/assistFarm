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

// 通过认证
export function agree(farmersId) {
  return request({
    url: '/assisting/farmers/agree/' + farmersId,
    method: 'put'
  })
}
