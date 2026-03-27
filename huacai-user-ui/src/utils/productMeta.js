/**
 * 电商化展示字段生成器（前端派生）
 * 说明：
 * - 不改后端接口字段，避免数据库迁移带来额外风险
 * - 通过 productsId/name/origin 等信息稳定生成：类目、销量、好评率、评价数
 * - 对同一商品保持稳定，刷新页面或重启项目不会随机跳动
 * - 当后端字段为 0 或空值时，优先使用更自然的展示兜底值
 */

function hashString(input) {
  const str = String(input || '')
  let hash = 2166136261
  for (let i = 0; i < str.length; i += 1) {
    hash ^= str.charCodeAt(i)
    hash = Math.imul(hash, 16777619)
  }
  return hash >>> 0
}

function pickCategory(product) {
  const name = String(product?.name || '')
  const edible = String(product?.edible || '')
  const specs = String(product?.specs || '')
  const mix = `${name} ${edible} ${specs}`

  const rules = [
    { cat: '水果生鲜', keys: ['橙', '梨', '苹果', '荔枝', '蜜柚', '猕猴桃', '枣', '芒果', '凤梨', '樱桃', '柚', '百香果', '石榴'] },
    { cat: '肉禽水产', keys: ['羊', '牛', '鸡', '鸭', '虾', '蟹', '海参', '鱼', '肉卷', '肉'] },
    { cat: '粮油米面', keys: ['大米', '稻花香', '米粉', '面', '玉米', '红薯', '紫薯', '芋头'] },
    { cat: '零食坚果', keys: ['核桃', '山核桃', '葡萄干', '花生', '牛肉干', '鲜花饼', '奶酪'] },
    { cat: '茶饮冲调', keys: ['茶', '普洱', '龙井', '毛峰', '咖啡', '原浆', '饮', '粥'] },
    { cat: '调味干货', keys: ['豆瓣', '海带', '木耳', '香菇', '贡菜', '汤包', '腊肠'] }
  ]

  const hit = rules.find(rule => rule.keys.some(key => mix.includes(key)))
  return hit?.cat || '助农优选'
}

function clamp(num, min, max) {
  return Math.max(min, Math.min(max, num))
}

function toPositiveNumber(value) {
  const number = Number(value)
  return Number.isFinite(number) && number > 0 ? number : null
}

export function withProductMeta(product) {
  const base = product || {}
  const seed = hashString(base.productsId || base.name || base.origin || base.image)

  const category = pickCategory(base)
  const price = Number(base.price || 0)

  // 销量：50 ~ 5000，并结合价格做轻微调整
  const priceFactor = clamp(1.1 - price / 500, 0.5, 1.15)
  const sales = Math.floor((50 + (seed % 4951)) * priceFactor)

  // 好评率：92.0% ~ 99.8%
  const rating = clamp(92 + (seed % 79) / 10, 92, 99.8)

  // 评价数：销量的一个比例，更贴近电商展示
  const reviewCount = clamp(Math.floor(sales * (0.06 + (seed % 20) / 1000)), 5, 99999)

  const backendCategory = base.category
  const backendSales = toPositiveNumber(base.sales)
  const backendRating = toPositiveNumber(base.rating)
  const backendReviewCount = toPositiveNumber(base.reviewCount ?? base.review_count)

  return {
    ...base,
    _meta: {
      category: backendCategory || category,
      sales: backendSales ?? sales,
      rating: backendRating ?? rating,
      reviewCount: backendReviewCount ?? reviewCount
    }
  }
}

export function getProductMeta(product) {
  if (product?._meta) return product._meta
  return withProductMeta(product)._meta
}
