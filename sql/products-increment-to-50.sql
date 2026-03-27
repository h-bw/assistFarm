SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================
-- 目标：把当前约 25 条商品增量补到 50 条
-- 用法：直接在当前业务库执行本脚本（无需重建库）
-- =====================================

-- 1) 电商化字段（兼容旧版 MySQL，不使用 ADD COLUMN IF NOT EXISTS）
SET @db_name = DATABASE();

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `products` ADD COLUMN `category` varchar(64) NULL DEFAULT NULL COMMENT ''类目（电商化展示）'';',
    'SELECT 1;'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'products' AND COLUMN_NAME = 'category'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `products` ADD COLUMN `sales` bigint NULL DEFAULT 0 COMMENT ''累计销量（电商化展示）'';',
    'SELECT 1;'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'products' AND COLUMN_NAME = 'sales'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `products` ADD COLUMN `rating` decimal(4,1) NULL DEFAULT 0.0 COMMENT ''好评率（百分比，例98.6）'';',
    'SELECT 1;'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'products' AND COLUMN_NAME = 'rating'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `products` ADD COLUMN `review_count` bigint NULL DEFAULT 0 COMMENT ''评价数（电商化展示）'';',
    'SELECT 1;'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'products' AND COLUMN_NAME = 'review_count'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `products` ADD COLUMN `gallery_images` longtext NULL COMMENT ''图集（JSON数组或逗号分隔URL）'';',
    'SELECT 1;'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'products' AND COLUMN_NAME = 'gallery_images'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `products` ADD COLUMN `shipping_promise` varchar(255) NULL DEFAULT NULL COMMENT ''发货承诺（48小时发货/冷链等）'';',
    'SELECT 1;'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'products' AND COLUMN_NAME = 'shipping_promise'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `products` ADD COLUMN `service_tags` varchar(255) NULL DEFAULT NULL COMMENT ''服务标签（逗号分隔）'';',
    'SELECT 1;'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'products' AND COLUMN_NAME = 'service_tags'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `products` ADD COLUMN `farmer_verified` tinyint NULL DEFAULT 0 COMMENT ''农户认证（1是0否）'';',
    'SELECT 1;'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'products' AND COLUMN_NAME = 'farmer_verified'
); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) 为确保可重复执行，先清理本批次 ID（026~050）
DELETE FROM `products`
WHERE `products_id` IN (
  'p202603260026','p202603260027','p202603260028','p202603260029','p202603260030',
  'p202603260031','p202603260032','p202603260033','p202603260034','p202603260035',
  'p202603260036','p202603260037','p202603260038','p202603260039','p202603260040',
  'p202603260041','p202603260042','p202603260043','p202603260044','p202603260045',
  'p202603260046','p202603260047','p202603260048','p202603260049','p202603260050'
);

-- 3) 补 25 条商品（026~050）
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260026', '福建武夷岩茶（大红袍）', '岩韵醇厚，回甘明显，耐泡', 168.00, '/.downloaded-images/福建武夷岩茶（大红袍）/01.jpg', '福建武夷山', '福建武夷山', 190.00, '125g礼盒', '常温24个月', '避光密封', '冲泡饮用', '<h2><strong>喝法</strong></h2><p>建议盖碗冲泡，前3泡快出汤更香。</p><p><img src=\"/.downloaded-images/福建武夷岩茶（大红袍）/02.jpg\" style=\"max-width:100%\" /></p>', 100, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260027', '四川眉山春见耙耙柑', '果肉细腻，甜度高，皮薄好剥', 39.90, '/.downloaded-images/四川眉山春见耙耙柑/01.jpg', '四川眉山', '四川眉山', 880.00, '5斤装', '常温7天，冷藏15天', '阴凉通风处存放', '即食', '<h2><strong>图文详情</strong></h2><p>入口化渣、酸甜平衡，办公室下午茶很适合。</p><p><img src=\"/.downloaded-images/四川眉山春见耙耙柑/02.jpg\" style=\"max-width:100%\" /></p><p><img src=\"/.downloaded-images/四川眉山春见耙耙柑/03.jpg\" style=\"max-width:100%\" /></p>', 103, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260028', '贵州猕猴桃（绿心）', '酸甜适口，维C丰富，早餐更清爽', 29.90, '/.downloaded-images/贵州猕猴桃（绿心）/01.jpg', '贵州修文', '贵州贵阳', 520.00, '10个装', '常温催熟后冷藏7天', '常温催熟后冷藏', '即食/榨汁', '<h2><strong>成熟提示</strong></h2><p>捏起来微微变软即可食用，口感更佳。</p><p><img src=\"/.downloaded-images/贵州猕猴桃（绿心）/02.jpg\" style=\"max-width:100%\" /></p>', 100, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260029', '云南甜玉米（真空）', '香甜软糯，拆袋即热更方便', 24.90, '/.downloaded-images/云南甜玉米（真空）/01.jpg', '云南曲靖', '云南曲靖', 820.00, '8根装', '冷藏7天，冷冻60天', '冷藏/冷冻保存', '加热食用', '<h2><strong>便捷</strong></h2><p>早餐 3 分钟搞定，带去办公室也很合适。</p><p><img src=\"/.downloaded-images/云南甜玉米（真空）/02.jpg\" style=\"max-width:100%\" /></p>', 103, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260030', '山东海捕海蛎子干', '鲜味足，煲汤更香', 69.90, '/.downloaded-images/山东海捕海蛎子干/01.jpg', '山东威海', '山东威海', 210.00, '200g袋装', '常温12个月', '阴凉干燥处存放', '泡发后煲汤', '<h2><strong>吃法</strong></h2><p>建议提前泡发 2-3 小时，煲粥/煲汤都很鲜。</p><p><img src=\"/.downloaded-images/山东海捕海蛎子干/02.jpg\" style=\"max-width:100%\" /></p>', 100, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260031', '云南野生菌汤包', '一包出汤，鲜香浓郁，适合火锅底', 39.90, '/.downloaded-images/云南野生菌汤包/01.jpg', '云南昆明', '云南昆明', 560.00, '3包组合', '常温12个月', '阴凉处存放', '煲汤/火锅', '<h2><strong>图文详情</strong></h2><p>混合菌菇风味更丰富，煲鸡汤/排骨汤都合适。</p><p><img src=\"/.downloaded-images/云南野生菌汤包/02.jpg\" style=\"max-width:100%\" /></p><p><img src=\"/.downloaded-images/云南野生菌汤包/03.jpg\" style=\"max-width:100%\" /></p>', 103, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260032', '江西赣南脐橙（精选）', '果香浓郁，汁水足，甜度更稳定', 39.90, '/.downloaded-images/江西赣南脐橙（精选）/01.jpg', '江西赣州', '江西赣州', 980.00, '5斤装', '常温10天，冷藏20天', '阴凉干燥处存放', '即食', '<h2><strong>购买建议</strong></h2><p>收到后放置 1-2 天更回甜，口感更好。</p><p><img src=\"/.downloaded-images/江西赣南脐橙（精选）/02.jpg\" style=\"max-width:100%\" /></p>', 100, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260033', '浙江龙井茶（雨前）', '清香鲜爽，回甘悠长', 158.00, '/.downloaded-images/浙江龙井茶（雨前）/01.jpg', '浙江杭州', '浙江杭州', 130.00, '100g罐装', '常温18个月', '避光密封', '冲泡饮用', '<h2><strong>参数</strong></h2><p>建议 80℃ 水温，先注水后投茶更鲜爽。</p><p><img src=\"/.downloaded-images/浙江龙井茶（雨前）/02.jpg\" style=\"max-width:100%\" /></p>', 103, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260034', '广西荔浦芋头', '粉糯香甜，做芋泥/炖菜都好吃', 29.90, '/.downloaded-images/广西荔浦芋头/01.jpg', '广西桂林荔浦', '广西桂林', 610.00, '5斤装', '常温15天', '阴凉通风处存放', '蒸/煮/炖', '<h2><strong>图文详情</strong></h2><p>蒸熟压成芋泥，加牛奶更香；炖排骨也很粉。</p><p><img src=\"/.downloaded-images/广西荔浦芋头/02.jpg\" style=\"max-width:100%\" /></p><p><img src=\"/.downloaded-images/广西荔浦芋头/03.jpg\" style=\"max-width:100%\" /></p>', 100, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260035', '云南贡菜（干）', '脆爽耐煮，火锅麻辣烫必备', 22.90, '/.downloaded-images/云南贡菜（干）/01.jpg', '云南大理', '云南大理', 700.00, '200g袋装', '常温12个月', '密封防潮', '泡发后食用', '<h2><strong>泡发方法</strong></h2><p>温水泡发 2-3 小时，口感更脆。</p><p><img src=\"/.downloaded-images/云南贡菜（干）/02.jpg\" style=\"max-width:100%\" /></p>', 103, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260036', '新疆阿克苏苹果', '果香足，脆甜多汁，皮薄', 39.90, '/.downloaded-images/新疆阿克苏苹果/01.jpg', '新疆阿克苏', '新疆阿克苏', 860.00, '5斤装', '常温20天', '阴凉干燥处存放', '即食', '<h2><strong>图文详情</strong></h2><p>咬一口“咔嚓”脆，汁水充足，适合全家分享。</p><p><img src=\"/.downloaded-images/新疆阿克苏苹果/02.jpg\" style=\"max-width:100%\" /></p><p><img src=\"/.downloaded-images/新疆阿克苏苹果/03.jpg\" style=\"max-width:100%\" /></p>', 100, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260037', '黑龙江五常大米（家庭装）', '米香浓郁，颗粒饱满，蒸饭更香', 99.90, '/.downloaded-images/黑龙江五常大米（家庭装）/01.jpg', '黑龙江五常', '黑龙江哈尔滨', 240.00, '5kg装', '常温12个月', '避光防潮', '蒸煮', '<h2><strong>口感</strong></h2><p>油润回甜，适合日常主食。</p><p><img src=\"/.downloaded-images/黑龙江五常大米（家庭装）/02.jpg\" style=\"max-width:100%\" /></p>', 103, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260038', '四川麻辣牛肉干', '麻辣过瘾，肉质紧实，越嚼越香', 49.90, '/.downloaded-images/四川麻辣牛肉干/01.jpg', '四川自贡', '四川自贡', 500.00, '250g袋装', '常温6个月', '阴凉干燥处存放', '即食', '<h2><strong>图文详情</strong></h2><p>精选牛后腿肉，香料更足，麻辣更带劲。</p><p><img src=\"/.downloaded-images/四川麻辣牛肉干/02.jpg\" style=\"max-width:100%\" /></p><p><img src=\"/.downloaded-images/四川麻辣牛肉干/03.jpg\" style=\"max-width:100%\" /></p>', 100, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260039', '广东潮汕牛肉丸', '弹嫩爆汁，火锅必点', 59.90, '/.downloaded-images/广东潮汕牛肉丸/01.jpg', '广东汕头', '广东汕头', 330.00, '500g袋装', '冷冻6个月', '-18℃冷冻', '火锅/煮汤', '<h2><strong>烹饪建议</strong></h2><p>水开下锅 3-5 分钟即可，过煮会变老。</p><p><img src=\"/.downloaded-images/广东潮汕牛肉丸/02.jpg\" style=\"max-width:100%\" /></p>', 103, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260040', '云南小粒咖啡豆', '果香明显，酸度柔和，手冲更适合', 88.00, '/.downloaded-images/云南小粒咖啡豆/01.jpg', '云南普洱', '云南普洱', 180.00, '250g袋装', '常温12个月', '避光密封', '研磨冲煮', '<h2><strong>风味描述</strong></h2><p>坚果/巧克力/果香风味更明显，适合日常手冲。</p><p><img src=\"/.downloaded-images/云南小粒咖啡豆/02.jpg\" style=\"max-width:100%\" /></p>', 100, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260041', '海南凤梨（甜心）', '果肉金黄，香甜多汁，刺少', 39.90, '/.downloaded-images/海南凤梨（甜心）/01.jpg', '海南三亚', '海南三亚', 260.00, '2个装', '常温5天', '阴凉通风处存放', '即食', '<h2><strong>图文详情</strong></h2><p>成熟度高，甜度更突出，适合做果盘/酸奶碗。</p><p><img src=\"/.downloaded-images/海南凤梨（甜心）/02.jpg\" style=\"max-width:100%\" /></p><p><img src=\"/.downloaded-images/海南凤梨（甜心）/03.jpg\" style=\"max-width:100%\" /></p>', 103, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260042', '云南紫薯', '粉糯香甜，做紫薯泥更细腻', 22.90, '/.downloaded-images/云南紫薯/01.jpg', '云南楚雄', '云南楚雄', 560.00, '5斤装', '常温10天', '阴凉通风处存放', '蒸/煮/烤', '<h2><strong>吃法</strong></h2><p>蒸熟压泥，加牛奶/椰奶更香。</p><p><img src=\"/.downloaded-images/云南紫薯/02.jpg\" style=\"max-width:100%\" /></p>', 100, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260043', '福建香菇（干货）', '香味浓郁，泡发率高，炖汤更香', 36.90, '/.downloaded-images/福建香菇（干货）/01.jpg', '福建宁德', '福建宁德', 460.00, '250g袋装', '常温12个月', '密封防潮', '泡发后食用', '<h2><strong>图文详情</strong></h2><p>剪柄泡发后更入味，炖鸡炖排骨都合适。</p><p><img src=\"/.downloaded-images/福建香菇（干货）/02.jpg\" style=\"max-width:100%\" /></p><p><img src=\"/.downloaded-images/福建香菇（干货）/03.jpg\" style=\"max-width:100%\" /></p>', 103, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260044', '山东即食海参（冷冻）', '肉质Q弹，营养丰富，送礼更体面', 299.00, '/.downloaded-images/山东即食海参（冷冻）/01.jpg', '山东青岛', '山东青岛', 80.00, '8只装', '冷冻12个月', '-18℃冷冻', '解冻即食', '<h2><strong>品质说明</strong></h2><p>冷链配送，收到后请尽快冷冻保存。</p><p><img src=\"/.downloaded-images/山东即食海参（冷冻）/02.jpg\" style=\"max-width:100%\" /></p>', 100, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260045', '贵州腊肠（烟熏）', '咸香下饭，肥瘦适中，炒饭更香', 49.90, '/.downloaded-images/贵州腊肠（烟熏）/01.jpg', '贵州遵义', '贵州遵义', 280.00, '500g袋装', '常温60天', '阴凉通风处存放', '蒸/炒', '<h2><strong>烹饪建议</strong></h2><p>先蒸 10 分钟再炒菜/炒饭，油脂更香。</p><p><img src=\"/.downloaded-images/贵州腊肠（烟熏）/02.jpg\" style=\"max-width:100%\" /></p>', 103, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260046', '云南普洱生茶（饼）', '花香明显，回甘持久，越陈越香', 198.00, '/.downloaded-images/云南普洱生茶（饼）/01.jpg', '云南西双版纳', '云南西双版纳', 120.00, '357g/饼', '常温长期保存', '通风避光', '冲泡饮用', '<h2><strong>图文详情</strong></h2><p>入口清爽，回甘快，适合长期存放。</p><p><img src=\"/.downloaded-images/云南普洱生茶（饼）/02.jpg\" style=\"max-width:100%\" /></p><p><img src=\"/.downloaded-images/云南普洱生茶（饼）/03.jpg\" style=\"max-width:100%\" /></p>', 100, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260047', '广西桂林米粉（干）', '细滑爽口，居家速煮', 24.90, '/.downloaded-images/广西桂林米粉（干）/01.jpg', '广西桂林', '广西桂林', 760.00, '5袋装', '常温12个月', '阴凉干燥处存放', '煮食', '<h2><strong>吃法</strong></h2><p>配卤水/酸笋/辣椒油更有风味。</p><p><img src=\"/.downloaded-images/广西桂林米粉（干）/02.jpg\" style=\"max-width:100%\" /></p>', 103, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260048', '福建金线莲茶', '清润回甘，日常养生更适合', 69.90, '/.downloaded-images/福建金线莲茶/01.jpg', '福建南平', '福建南平', 220.00, '60g罐装', '常温18个月', '避光密封', '冲泡饮用', '<h2><strong>冲泡</strong></h2><p>90℃热水冲泡 5-8 分钟即可饮用，可反复冲泡。</p><p><img src=\"/.downloaded-images/福建金线莲茶/02.jpg\" style=\"max-width:100%\" /></p>', 100, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260049', '青海枸杞原浆', '酸甜适口，随身小支装更方便', 79.90, '/.downloaded-images/青海枸杞原浆/01.jpg', '青海海西', '青海海西', 360.00, '10支装', '常温12个月', '阴凉处存放', '开封即饮', '<h2><strong>图文详情</strong></h2><p>小支装随身带，早餐/加班都能快速补充能量。</p><p><img src=\"/.downloaded-images/青海枸杞原浆/02.jpg\" style=\"max-width:100%\" /></p><p><img src=\"/.downloaded-images/青海枸杞原浆/03.jpg\" style=\"max-width:100%\" /></p>', 103, '2026-03-26 21:20:00');
INSERT INTO `products` (`products_id`,`name`,`subtitle`,`price`,`image`,`origin`,`shipFrom`,`inventory`,`specs`,`expire`,`storage`,`edible`,`detail`,`user_id`,`create_time`) VALUES ('p202603260050', '山东黄河口大闸蟹礼盒', '蟹黄饱满，肉质鲜甜，送礼体面', 268.00, '/.downloaded-images/山东黄河口大闸蟹礼盒/01.jpg', '山东东营', '山东东营', 90.00, '6只礼盒', '冷藏3天', '冷藏保湿', '需蒸煮', '<h2><strong>图文详情</strong></h2><p>蒸熟后蟹香四溢，建议搭配姜醋汁更提鲜。</p><p><img src=\"/.downloaded-images/山东黄河口大闸蟹礼盒/02.jpg\" style=\"max-width:100%\" /></p><p><img src=\"/.downloaded-images/山东黄河口大闸蟹礼盒/03.jpg\" style=\"max-width:100%\" /></p><h2><strong>售后</strong></h2><p>冷链运输到家，若有死蟹请按规则售后处理。</p>', 100, '2026-03-26 21:20:00');

-- 4) 图集兜底：为空的商品自动补 4 张图
UPDATE `products`
SET `gallery_images` = JSON_ARRAY(
  CONCAT('/.downloaded-images/', `name`, '/02.jpg'),
  CONCAT('/.downloaded-images/', `name`, '/03.jpg'),
  CONCAT('/.downloaded-images/', `name`, '/04.jpg'),
  CONCAT('/.downloaded-images/', `name`, '/05.jpg')
)
WHERE (`gallery_images` IS NULL OR `gallery_images` = '');

SET FOREIGN_KEY_CHECKS = 1;
