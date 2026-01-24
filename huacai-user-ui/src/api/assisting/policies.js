import request from '@/utils/request'

// 查询助农政策列表
export function listPolicies(query) {
  return request({
    url: '/assisting/policies/list',
    method: 'get',
    params: query
  })
}

// 查询助农政策详细
export function getPolicies(policiesId) {
  return request({
    url: '/assisting/policies/' + policiesId,
    method: 'get'
  })
}

// 新增助农政策
export function addPolicies(data) {
  return request({
    url: '/assisting/policies',
    method: 'post',
    data: data
  })
}

// 修改助农政策
export function updatePolicies(data) {
  return request({
    url: '/assisting/policies',
    method: 'put',
    data: data
  })
}

// 删除助农政策
export function delPolicies(policiesId) {
  return request({
    url: '/assisting/policies/' + policiesId,
    method: 'delete'
  })
}
