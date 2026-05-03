SET NAMES utf8mb4;
SOURCE D:/DevelopSoft/assistFarm/sql/policies-summary-update.sql;
SOURCE D:/DevelopSoft/assistFarm/sql/policies-compact-content-update.sql;
UPDATE policies SET title = '商务部等九部门关于推动农村电商高质量发展的实施意见' WHERE policies_id = 'demo_policy_20260324_01';
UPDATE policies SET title = '北京市关于促进农产品市场发展的若干措施' WHERE policies_id = 'demo_policy_20260324_02';
UPDATE policies SET title = '宁夏“数商兴农年”活动工作安排' WHERE policies_id = 'demo_policy_20260324_03';
UPDATE policies SET title = '科技特派员助力乡村振兴服务案例' WHERE policies_id = 'demo_policy_20260324_04';
UPDATE policies SET title = '促进农产品消费实施方案' WHERE policies_id = 'demo_policy_20260324_05';
