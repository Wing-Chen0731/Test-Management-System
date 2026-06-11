CREATE DATABASE IF NOT EXISTS test_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE test_management;

CREATE TABLE IF NOT EXISTS test_case (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(255) NOT NULL COMMENT '测试用例标题',
    module VARCHAR(100) COMMENT '所属模块',
    priority VARCHAR(20) COMMENT '优先级：高/中/低',
    precondition TEXT COMMENT '前置条件',
    steps TEXT COMMENT '测试步骤',
    expected_result TEXT COMMENT '预期结果',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT/READY/PASSED/FAILED',
    creator VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_module (module),
    INDEX idx_status (status),
    INDEX idx_creator (creator)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试用例表';
