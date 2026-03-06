// src/api/assisting/recommend.js
import request from '@/utils/request'

// 获取用户个性化推荐商品ID列表
export function getRecommendations(userId, topN = 6) {
    return request({
        url: '/assisting/recommend/user/' + userId,
        method: 'get',
        params: { topN }
    })
}

// 根据商品ID列表批量查询商品详情
export function getProductListByIds(ids) {
    return request({
        url: '/assisting/products/listByIds',
        method: 'post',
        data: ids
    })
}