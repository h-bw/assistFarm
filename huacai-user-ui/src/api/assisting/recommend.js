import request from '@/utils/request'

export function getRecommendations(userId, topN = 6, options = {}) {
  const params = { topN }

  if (options.scene) {
    params.scene = options.scene
  }

  if (Array.isArray(options.productIds) && options.productIds.length > 0) {
    params.productIds = options.productIds.join(',')
  }

  return request({
    url: '/assisting/recommend/user/' + userId,
    method: 'get',
    params
  })
}

export function getProductListByIds(ids) {
  return request({
    url: '/assisting/products/listByIds',
    method: 'post',
    data: ids
  })
}

export function mergeRecommendationProducts(items = [], products = []) {
  const reasonMap = new Map(items.map(item => [item.productsId, item.reason]))
  return (products || []).map(product => ({
    ...product,
    recommendReason: reasonMap.get(product.productsId) || ''
  }))
}
