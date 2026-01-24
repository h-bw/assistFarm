import request from '@/utils/request'

// 查询农户产品列表
export function listProducts(query) {
  return request({
    url: '/assisting/products/list',
    method: 'get',
    params: query
  })
}

// 查询农户产品详细
export function getProducts(productsId) {
  return request({
    url: '/assisting/products/' + productsId,
    method: 'get'
  })
}

// 新增农户产品
export function addProducts(data) {
  return request({
    url: '/assisting/products',
    method: 'post',
    data: data
  })
}

// 修改农户产品
export function updateProducts(data) {
  return request({
    url: '/assisting/products',
    method: 'put',
    data: data
  })
}

// 删除农户产品
export function delProducts(productsId) {
  return request({
    url: '/assisting/products/' + productsId,
    method: 'delete'
  })
}

/**
 * 查询农户产品列表(无数据权限)
 */
export function selectList(query) {
  return request({
    url: '/assisting/products/selectList',
    method: 'get',
    params: query
  })
}
