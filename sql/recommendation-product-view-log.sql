-- 商品详情浏览行为表
-- 用于记录用户在商品详情页的浏览行为，便于后续接入推荐算法的弱兴趣特征

CREATE TABLE IF NOT EXISTS product_view_log (
    view_id         varchar(64)  NOT NULL COMMENT '浏览记录ID',
    user_id         bigint       NOT NULL COMMENT '用户ID',
    products_id     varchar(64)  NOT NULL COMMENT '商品ID',
    view_source     varchar(32)  DEFAULT 'detail' COMMENT '浏览来源',
    view_count      int          DEFAULT 1 COMMENT '累计浏览次数',
    last_view_time  datetime     NOT NULL COMMENT '最近浏览时间',
    create_time     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (view_id),
    UNIQUE KEY uk_view_user_product (user_id, products_id),
    KEY idx_view_user_time (user_id, last_view_time),
    KEY idx_view_product_time (products_id, last_view_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品详情浏览行为记录表';
