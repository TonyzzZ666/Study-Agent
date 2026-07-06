-- 学习养成计划 数据库初始化脚本
-- 使用方法：在 MySQL 中执行此脚本即可

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `study_habit` DEFAULT CHARACTER SET utf8mb4;
USE `study_habit`;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `nick_name` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '启用状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 任务表
-- ============================================
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '任务标题',
    `description` TEXT COMMENT '任务描述',
    `priority` ENUM('HIGH','MEDIUM','LOW') DEFAULT 'MEDIUM' COMMENT '优先级',
    `deadline` DATETIME NOT NULL COMMENT '截止时间',
    `need_review` TINYINT(1) DEFAULT 0 COMMENT '需复习提醒',
    `status` ENUM('TODO','DONE') DEFAULT 'TODO' COMMENT '任务状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- ============================================
-- 3. 打卡记录表
-- ============================================
DROP TABLE IF EXISTS `check_in`;
CREATE TABLE `check_in` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '打卡ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `check_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_user_time` (`user_id`, `check_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

-- ============================================
-- 4. 推荐模板表
-- ============================================
DROP TABLE IF EXISTS `recommendation`;
CREATE TABLE `recommendation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `title` VARCHAR(200) NOT NULL COMMENT '模板标题',
    `description` TEXT COMMENT '模板描述',
    `priority` ENUM('HIGH','MEDIUM','LOW') DEFAULT 'MEDIUM' COMMENT '优先级',
    `category` VARCHAR(50) DEFAULT '通用' COMMENT '分类',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐模板表';

-- ============================================
-- 预置推荐模板数据
-- ============================================
INSERT INTO `recommendation` (`title`, `description`, `priority`, `category`) VALUES
('每日背单词', '每天背诵30个英语单词，坚持打卡积累词汇量', 'MEDIUM', '英语'),
('每日刷算法题', '每天完成1道LeetCode算法题，提升编程能力', 'HIGH', '编程'),
('每日阅读', '每天阅读技术书籍或文献30分钟，拓展知识面', 'MEDIUM', '阅读'),
('周复习计划', '每周日复习本周所学全部内容，巩固记忆', 'HIGH', '复习');
