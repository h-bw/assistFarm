SET @column_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'policies'
    AND COLUMN_NAME = 'source_link'
);

SET @ddl := IF(
  @column_exists = 0,
  'ALTER TABLE `policies` ADD COLUMN `source_link` varchar(500) NULL COMMENT ''政策来源链接'' AFTER `address`',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
