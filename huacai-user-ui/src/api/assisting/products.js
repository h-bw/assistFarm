import request from '@/utils/request'

// 查询农户产品列表
export function listProducts(query) {
  return request({
    url: '/assisting/products/list',
    method: 'get',
    params: query
  })
}

// 查询农户产品详情
export function getProducts(productsId) {
  return request({
    url: '/assisting/products/' + productsId,
    method: 'get'
  })
}

// 上报商品详情浏览行为
export function reportProductView(productsId) {
  return request({
    url: '/assisting/products/' + productsId + '/view',
    method: 'post'
  })
}

// 新增农户产品
export function addProducts(data) {
  return request({
    url: '/assisting/products',
    method: 'post',
    data
  })
}

// 修改农户产品
export function updateProducts(data) {
  return request({
    url: '/assisting/products',
    method: 'put',
    data
  })
}

// 删除农户产品
export function delProducts(productsId) {
  return request({
    url: '/assisting/products/' + productsId,
    method: 'delete'
  })
}

// 查询农户产品列表（无数据权限）
export function selectList(query) {
  return request({
    url: '/assisting/products/selectList',
    method: 'get',
    params: query
  })
}
