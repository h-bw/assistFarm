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

// 支付订单
export function payment(ordersIds) {
  return request({
    url: '/assisting/orders/payment/' + ordersIds,
    method: 'put'
  })
}

// 鏌ヨ鏀粯缁撴灉骞舵洿鏂拌鍗曠姸鎬?
export function queryPaymentStatus(ordersId) {
  return request({
    url: `/api/pay/query/${ordersId}`,
    method: 'get'
  })
}
