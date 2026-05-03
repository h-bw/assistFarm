-- 推荐算法扩展设计：商品详情浏览行为
-- 目标：为后续“浏览详情 = 0.3”权重接入提供数据结构基础

DROP TABLE IF EXISTS product_view_log;

CREATE TABLE product_view_log (
    view_id           varchar(64)    NOT NULL COMMENT '浏览记录主键',
    user_id           bigint         NOT NULL COMMENT '用户ID',
    products_id       varchar(64)    NOT NULL COMMENT '商品ID',
    view_source       varchar(32)    DEFAULT 'detail' COMMENT '浏览来源：首页推荐、商品列表、商品详情等',
    view_count        int            DEFAULT 1 COMMENT '累计浏览次数',
    last_view_time    datetime       NOT NULL COMMENT '最近浏览时间',
    create_time       datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '首次记录时间',
    update_time       datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (view_id),
    KEY idx_view_user_time (user_id, last_view_time),
    KEY idx_view_user_product (user_id, products_id),
    KEY idx_view_product_time (products_id, last_view_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品详情浏览行为记录表';

-- 推荐查询示例：读取某个用户最近浏览过的商品，作为弱兴趣输入
-- 建议在推荐服务中限制条数，例如最近 20 条，避免弱行为过度放大
SELECT products_id
FROM product_view_log
WHERE user_id = 1
GROUP BY products_id
ORDER BY MAX(last_view_time) DESC
LIMIT 20;

-- 写入策略建议：
-- 1. 首次浏览：插入一条记录
-- 2. 重复浏览：更新 view_count 和 last_view_time
-- 3. 同一用户同一商品可做幂等合并，避免日志无限膨胀
