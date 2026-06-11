CREATE TABLE IF NOT EXISTS test_case (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    module VARCHAR(100),
    priority VARCHAR(20),
    precondition TEXT,
    steps TEXT,
    expected_result TEXT,
    status VARCHAR(20) DEFAULT 'DRAFT',
    creator VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);
