import request from '@/utils/request'

// 查询收货地址列表
export function listAddresses(query) {
  return request({
    url: '/assisting/addresses/list',
    method: 'get',
    params: query
  })
}

// 查询收货地址详细
export function getAddresses(addressesId) {
  return request({
    url: '/assisting/addresses/' + addressesId,
    method: 'get'
  })
}

// 新增收货地址
export function addAddresses(data) {
  return request({
    url: '/assisting/addresses',
    method: 'post',
    data: data
  })
}

// 修改收货地址
export function updateAddresses(data) {
  return request({
    url: '/assisting/addresses',
    method: 'put',
    data: data
  })
}

// 删除收货地址
export function delAddresses(addressesId) {
  return request({
    url: '/assisting/addresses/' + addressesId,
    method: 'delete'
  })
}
