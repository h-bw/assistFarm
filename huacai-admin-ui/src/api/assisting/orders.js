import request from '@/utils/request'

// 查询订单列表
export function listOrders(query) {
  return request({
    url: '/assisting/orders/list',
    method: 'get',
    params: query
  })
}

// 查询订单详细
export function getOrders(ordersId) {
  return request({
    url: '/assisting/orders/' + ordersId,
    method: 'get'
  })
}

// 新增订单
export function addOrders(data) {
  return request({
    url: '/assisting/orders',
    method: 'post',
    data: data
  })
}

// 修改订单
export function updateOrders(data) {
  return request({
    url: '/assisting/orders',
    method: 'put',
    data: data
  })
}

// 删除订单
export function delOrders(ordersId) {
  return request({
    url: '/assisting/orders/' + ordersId,
    method: 'delete'
  })
}

//发货
export function sendOutGoods(data) {
  return request({
    url: '/assisting/orders/sendOutGoods',
    method: 'put',
    data: data
  })
}
